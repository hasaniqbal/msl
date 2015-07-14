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

import com.netflix.msl.entityauth.RsaAuthenticationData;

import mslcli.common.CmdArguments;
import mslcli.common.IllegalCmdArgumentException;
import mslcli.common.MslConfig;
import mslcli.common.util.AppContext;
import mslcli.common.util.ConfigurationException;
import mslcli.server.util.ServerAuthenticationUtils;

/**
 * <p>The configuration class for MSl server</p>
 * 
 * @author Vadim Spector <vspector@netflix.com>
 */

public final class ServerMslConfig extends MslConfig {
    public ServerMslConfig(final AppContext appCtx, final String serverId)
        throws ConfigurationException, IllegalCmdArgumentException
    {
        super(appCtx,
              new CmdArguments(new String[0]),
              serverId,
              new RsaAuthenticationData(serverId, appCtx.getRsaKeyId(serverId)),
              new ServerAuthenticationUtils(appCtx, serverId)
             );
    }
}
