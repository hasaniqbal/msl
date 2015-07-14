/**
 * Copyright (c) 2014 Netflix, Inc.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mslcli.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.netflix.msl.MslError;
import com.netflix.msl.MslException;

import mslcli.common.util.ConfigurationException;
import mslcli.common.util.ConfigurationRuntimeException;
import mslcli.common.util.MslProperties;
import mslcli.common.util.SharedUtil;

/**
 * Simple HTTP Server using com.sun.net.httpserver.* classes built into Oracle's JVM
 *
 * @author Vadim Spector <vspector@netflix.com>
 */

public class SimpleHttpServer {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Parameters: config_file");
            System.exit(1);
        }
        try {
            final MslProperties prop = MslProperties.getInstance(SharedUtil.loadPropertiesFromFile(args[0]));
            final SimpleMslServer mslServer = new SimpleMslServer(prop);
            final HttpServer server = HttpServer.create(new InetSocketAddress(prop.getServerPort()), 0);
            server.createContext("/msl", new MyHandler(mslServer));
            server.setExecutor(null); // creates a default executor
            System.out.println(String.format("waiting for requests on http://localhost:%d/msl ...", prop.getServerPort()));
            server.start();
        } catch (ConfigurationException e) {
            System.err.println("Server Configuration Error: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Server Initialization Error: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Server Internal Error: " + e.getMessage());
            SharedUtil.getRootCause(e).printStackTrace(System.err);
            System.exit(1);
        }
    }

    static class MyHandler implements HttpHandler {
        MyHandler(final SimpleMslServer mslServer) {
            this.mslServer = mslServer;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            System.out.println("Processing request");

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                // Allow requests from anywhere.
                t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                mslServer.processRequest(t.getRequestBody(), out);
            } catch (ConfigurationException e) {
                System.err.println("Server Configuration Error: " + e.getMessage());
            } catch (ConfigurationRuntimeException e) {
                System.err.println("Server Configuration Error: " + e.getCause().getMessage());
            } catch (MslException e) {
                System.err.println(SharedUtil.getMslExceptionInfo(e));
            } catch (IOException e) {
                final Throwable thr = SharedUtil.getRootCause(e);
                System.err.println("\nIO-ERROR: " + e);
                System.err.println("ROOT CAUSE:");
                thr.printStackTrace(System.err);
            } catch (RuntimeException e) {
                System.err.println("\nRT-ERROR: " + e);
                System.err.println("ROOT CAUSE:");
                SharedUtil.getRootCause(e).printStackTrace(System.err);
            } finally {
                final byte[] response = out.toByteArray();
                t.sendResponseHeaders(200, response.length);
                final OutputStream os = t.getResponseBody();
                os.write(response);
                os.flush();
                os.close();
            }

            System.out.println("\nSUCCESS!!!\n");
        }

        private final SimpleMslServer mslServer;
    }
}
