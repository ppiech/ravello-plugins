package com.ravello.plugins.maven.mojos;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.ravello.plugins.common.IOService;
import com.ravello.plugins.common.impl.IOServiceImpl;
import com.ravello.plugins.exceptions.ApplicationPropertiesException;
import com.ravello.plugins.exceptions.ApplicationPropertiesNotFoundException;
import com.ravello.plugins.maven.ArtifactResolverHelper;
import com.ravello.plugins.maven.MavenHelper;
import com.ravello.plugins.maven.PluginHelper;
import com.ravello.plugins.maven.impl.MavenHelperImpl;
import com.ravello.plugins.maven.impl.PluginArtifactResolver;

@Mojo(name = "inject-properties", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class InjectPropertiesMojo extends RavelloMojo {

	@Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true)
	protected List<ArtifactRepository> remoteRepositories;

	@Parameter(defaultValue = "${localRepository}", readonly = true, required = true)
	private ArtifactRepository localRepository;

	@Component
	protected ArtifactResolver resolver;

	@Parameter(defaultValue = "${reactorProjects}", readonly = true)
	private List<MavenProject> reactorProjects;

	@Parameter(defaultValue = "${plugin}", readonly = true)
	private PluginDescriptor pluginDescriptor;

	private static final String ARTIFACT_ID_PEFIX = "ravello-app";

	@Parameter(property = "propertiesFileName", required = true)
	protected String propertiesFileName;

	@Parameter(property = "propertiesMap", required = true)
	protected Map<String, String> propertiesMap;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {
			MavenHelper mavenHelper = new MavenHelperImpl(project,
					reactorProjects);
			List<PluginHelper> plugins = mavenHelper.findAllPlugins();
			ArtifactResolverHelper mvnArtifactResolver = new PluginArtifactResolver(
					pluginDescriptor, resolver, remoteRepositories,
					localRepository);
			File propertiesZip = mvnArtifactResolver
					.artifactToFile(ARTIFACT_ID_PEFIX);
			IOService ioService = new IOServiceImpl();
			ioService.unzipFile(propertiesZip, getTarget());
			Properties properties = ioService.readProperties(new File(
					getTarget(), propertiesFileName));
			Map<String, List<String>> maps = mavenHelper.preparePropertiesMap(
					propertiesMap, properties);
			mavenHelper.updateConfiguration(maps, properties, plugins);
			mavenHelper.updateProperties(maps, properties);
		} catch (ApplicationPropertiesException e) {
			throw new MojoFailureException(e.getMessage(), e);
		} catch (ApplicationPropertiesNotFoundException e) {
			throw new MojoFailureException(e.getMessage(), e);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

}
