/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.alarm.action.service.integration.test;

import java.io.File;

import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import com.ericsson.oss.itpf.sdk.security.accesscontrol.classic.AccessControlServiceMockImpl;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.classic.SecurityPrivilegeServiceMock;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.classic.TargetGroupRegistry;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.classic.TargetGroupsProviderMock;
import com.ericsson.oss.services.alarm.action.service.integration.base.DummyDataCreator;
import com.ericsson.oss.services.alarm.action.service.integration.util.AuthenticationHandler;
import com.ericsson.oss.services.alarm.action.service.integration.util.MediationTaskRequestListener;
import com.ericsson.oss.services.alarm.action.service.integration.util.ProxyBeanProvider;
import com.ericsson.oss.services.alarm.action.service.integration.util.ProxyBeanProviderBeanImpl;
import com.ericsson.oss.services.fm.alarmactionservice.util.AlarmActionConstants;
import com.ericsson.oss.services.fm.common.addinfo.CorrelationType;

@ArquillianSuiteDeployment
public class TestDeployment {

    @Deployment(order = 1)
    public static Archive<?> createTestEarDeployment() {
        final EnterpriseArchive testEAR = ShrinkWrap.create(EnterpriseArchive.class, "testAlarmActionService.ear");
        final JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "testAlarmActionService.jar");
        final WebArchive testWar = ShrinkWrap.create(WebArchive.class, "testAlarmActionService.war");
        // Add beans.xml to enable CDI
        testJar.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        testJar.addClass(AlarmActionServiceIT.class);
        testJar.addClass(AlarmActionUpdateTBACIT.class);
        testJar.addClass(ProxyBeanProvider.class);
        testJar.addClass(ProxyBeanProviderBeanImpl.class);
        testJar.addClass(DummyDataCreator.class);
        testJar.addClass(AuthenticationHandler.class);
        testJar.addClass(MediationTaskRequestListener.class);
        testJar.addClass(SecurityPrivilegeServiceMock.class);
        testJar.addClass(TargetGroupsProviderMock.class);
        testJar.addClass(TargetGroupRegistry.class);
        testJar.addClass(AccessControlServiceMockImpl.class);
        testJar.addClass(CorrelationType.class);
        testJar.addClass(AlarmActionConstants.class);

        final File[] accessControlServiceApi = Maven.resolver().loadPomFromFile("pom.xml").resolve("com.ericsson.oss.services.security.accesscontrol:access-control-service-api")
                .withoutTransitivity().asFile();
        final File[] alarmActionServiceApi = Maven.resolver().loadPomFromFile("pom.xml").resolve("com.ericsson.nms.services:AlarmActionService-api")
                .withoutTransitivity().asFile();
        final File[] fmmediationeventmodelJar = Maven.resolver().loadPomFromFile("pom.xml").resolve("com.ericsson.oss.services.fm.models:fmmediationeventmodel-jar")
                .withoutTransitivity().asFile();
        final File[] coreMediationApiJar = Maven.resolver().loadPomFromFile("pom.xml").resolve("com.ericsson.nms.mediation:core-mediation-api")
                .withoutTransitivity().asFile();


        // Add the jboss deployment structure xml
        testEAR.addAsManifestResource(new File("./target/test-classes/jboss-deployment-structure.xml"));
        // Add the jboss ejb3 descriptor xml

        final File[] http_client_osgi_jar = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("org.apache.httpcomponents:httpclient-osgi").withoutTransitivity().asFile();
        final File[] http_core_jar = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("org.apache.httpcomponents:httpcore").withoutTransitivity().asFile();
        final File[] http_client_jar = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("org.apache.httpcomponents:httpclient").withoutTransitivity().asFile();
        final File[] http_mime_jar = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("org.apache.httpcomponents:httpmime").withoutTransitivity().asFile();
        final File[] resteasy_jaxrs = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("org.jboss.resteasy:resteasy-jaxrs").withoutTransitivity().asFile();

        testEAR.addAsLibraries(http_client_osgi_jar);
        testEAR.addAsLibraries(http_client_jar);
        testEAR.addAsLibraries(http_core_jar);
        testEAR.addAsLibraries(http_mime_jar);
        testEAR.addAsLibraries(resteasy_jaxrs);

        testEAR.addAsLibraries(accessControlServiceApi);
        testEAR.addAsLibraries(alarmActionServiceApi);
        testEAR.addAsLibraries(fmmediationeventmodelJar);
        testEAR.addAsLibraries(coreMediationApiJar);

        testEAR.addAsModule(testJar);
        testEAR.addAsModule(testWar);
        return testEAR;
    }
}
