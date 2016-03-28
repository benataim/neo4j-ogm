/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.testutil;

import static org.neo4j.bolt.BoltKernelExtension.Settings.connector;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.bolt.BoltKernelExtension;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.service.Components;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 * @author vince
 */
public class MultiDriverTestClass {

    private static TestServer testServer;
    private static GraphDatabaseService impermanentDb;
    private static ServerControls boltServer;

    @BeforeClass
    public static void setupMultiDriverTestEnvironment() {

        if (Components.driver() instanceof HttpDriver ) {
            if (Components.neo4jVersion() < 2.2) {
                testServer = new TestServer.Builder()
                        .enableAuthentication(false)
                        .transactionTimeoutSeconds(2)
                        .build();
            } else {
                testServer = new TestServer.Builder()
                        .enableAuthentication(true)
                        .transactionTimeoutSeconds(2)
                        .build();
            }
        }
        else if (Components.driver() instanceof BoltDriver) {
            boltServer = TestServerBuilders.newInProcessBuilder()
                    .withConfig( connector(0, BoltKernelExtension.Settings.enabled), "true")
                    .newServer();
        }
        else {
            impermanentDb = new TestGraphDatabaseFactory().newImpermanentDatabase();
            Components.setDriver(new EmbeddedDriver(impermanentDb));
        }
    }

    @AfterClass
    public static void tearDownMultiDriverTestEnvironment() {
        if (testServer != null) {
            testServer.shutdown();
            testServer = null;
        }
        if (impermanentDb != null) {
            impermanentDb.shutdown();
            impermanentDb = null;
        }
        if (boltServer != null) {
            boltServer.close();
            boltServer = null;
        }
    }

    public static GraphDatabaseService getGraphDatabaseService() {
        if (testServer != null) {
            return testServer.getGraphDatabaseService();
        }
        if (boltServer != null) {
            return boltServer.graph();
        }
        return impermanentDb;
    }

    public static ServerControls getBoltServer() {
        return boltServer;
    }
}