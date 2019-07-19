/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.build.packagedocs;

import java.io.File;
import java.io.FileInputStream;
import java.time.Year;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.jvm.tasks.Jar;

import org.springframework.build.javadoc.JavadocPlugin;
import org.springframework.build.refdoc.RefdocPlugin;

/**
 * @author Brian Clozel
 */
public class PackageDocsPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		Zip docsZipTask = createDocsZipTask(project);
		Zip schemaZipTask = createSchemaZipTask(project);

		Zip distZipTask = createDistZipTask(project, docsZipTask, schemaZipTask);
		distZipTask.dependsOn(docsZipTask, schemaZipTask);
	}

	private Zip createDocsZipTask(Project project) {
		Task javadocApi = project.getTasks().getByName(JavadocPlugin.JAVADOC_API_TASK_NAME);
		Task kotlinApi = project.getTasks().getByName(JavadocPlugin.KOTLIN_API_TASK_NAME);
		Task refDocs = project.getTasks().getByName(RefdocPlugin.REFDOC_TASK_NAME);

		Zip docsZip = project.getTasks().create("docsZip", Zip.class);
		docsZip.setGroup("Distribution");
		docsZip.getArchiveBaseName().set("spring-framework");
		docsZip.getArchiveClassifier().set("docs");
		docsZip.setDescription("Builds docs archive containing api and reference " +
				"for deployment at https://docs.spring.io/spring-framework/docs.");

		docsZip.from(javadocApi.getOutputs()).into("javadoc-api");
		docsZip.from(kotlinApi.getOutputs()).into("kdoc-api");
		docsZip.from(refDocs).into("spring-framework-reference");

		docsZip.dependsOn(javadocApi, kotlinApi, refDocs);
		return docsZip;
	}

	private Zip createSchemaZipTask(Project project) {
		Zip schemaZip = project.getTasks().create("schemaZip", Zip.class);
		schemaZip.setGroup("Distribution");
		schemaZip.getArchiveBaseName().set("spring-framework");
		schemaZip.getArchiveClassifier().set("schema");
		schemaZip.setDescription("Builds schema archive containing all " +
				"XSDs for deployment at https://springframework.org/schema.");
		schemaZip.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);

		project.getSubprojects().forEach(p -> {
			Properties schemas = loadSchemas(p);
			for (Object key : schemas.keySet()) {
				String shortName = key.toString().replaceAll("http.*schema.(.*).spring-.*", "$1");
				assert shortName != key;
				File xsdFile = filterMainSourceSet(p, schemas.get(key.toString()).toString()).getSingleFile();
				schemaZip.into(shortName).from(xsdFile.getAbsolutePath());
			}
		});

		return schemaZip;
	}

	private Properties loadSchemas(Project p) {
		Properties schemas = new Properties();
		JavaPluginConvention java = p.getConvention().getPlugin(JavaPluginConvention.class);
		Iterator<File> schemasLists = filterMainSourceSet(p, "META-INF/spring.schemas").iterator();
		if (schemasLists.hasNext()) {
			File schemaList = schemasLists.next();
			try (FileInputStream is = new FileInputStream(schemaList)) {
				schemas.load(is);
			}
			catch (Exception e) {
			}
		}
		return schemas;
	}

	private FileCollection filterMainSourceSet(Project p, String pathEndingWith) {
		JavaPluginConvention java = p.getConvention().getPlugin(JavaPluginConvention.class);
		return java.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME).getResources()
				.filter(file -> file.getPath().endsWith(pathEndingWith));
	}

	private Zip createDistZipTask(Project project, Zip docsZip, Zip schemaZip) {
		Zip distZip = project.getTasks().create("distZip", Zip.class);
		distZip.setGroup("Distribution");
		distZip.getArchiveBaseName().set("spring-framework");
		distZip.getArchiveClassifier().set("dist");
		distZip.setDescription("Builds dist archive, containing all jars and docs, " +
				"suitable for community download page.");

		String baseDirName = "spring-framework-" + project.getVersion().toString();

		Map<String, String> values = new HashMap<>();
		values.put("copyright", Integer.toString(Year.now().getValue()));
		values.put("version", project.getVersion().toString());

		distZip.from("src/docs/dist", spec -> {
			spec.include("readme.txt", "license.txt", "notice.txt")
					.into(baseDirName)
					.expand(values);
		});

		distZip.from(project.zipTree(docsZip.getArchiveFile())).into(baseDirName + "/docs");
		distZip.from(project.zipTree(schemaZip.getArchiveFile())).into(baseDirName + "/schema");
		project.subprojects(p -> {
			List<FileCollection> jars = p.getTasks()
					.withType(Jar.class)
					.stream()
					.map(jar -> jar.getOutputs().getFiles())
					.collect(Collectors.toList());
			distZip.from(jars).into(baseDirName + "/libs");
		});
		return distZip;
	}

}
