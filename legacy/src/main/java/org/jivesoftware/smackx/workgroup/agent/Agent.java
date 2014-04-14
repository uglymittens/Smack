/**
 *
 * Copyright 2003-2007 Jive Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.smackx.workgroup.agent;

import org.jivesoftware.smackx.workgroup.packet.AgentInfo;
import org.jivesoftware.smackx.workgroup.packet.AgentWorkgroups;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.IQ;

import java.util.Collection;

/**
 * The <code>Agent</code> class is used to represent one agent in a Workgroup Queue.
 *
 * @author Derek DeMoro
 */
public class Agent {
    private XMPPConnection connection;
    private String workgroupJID;

    public static Collection<String> getWorkgroups(String serviceJID, String agentJID, XMPPConnection connection) throws NoResponseException, XMPPErrorException, NotConnectedException {
        AgentWorkgroups request = new AgentWorkgroups(agentJID);
        request.setTo(serviceJID);
        AgentWorkgroups response = (AgentWorkgroups) connection.createPacketCollectorAndSend(request).nextResultOrThrow();
        return response.getWorkgroups();
    }

    /**
     * Constructs an Agent.
     */
    Agent(XMPPConnection connection, String workgroupJID) {
        this.connection = connection;
        this.workgroupJID = workgroupJID;
    }

    /**
     * Return the agents JID
     *
     * @return - the agents JID.
     */
    public String getUser() {
        return connection.getUser();
    }

    /**
     * Return the agents name.
     *
     * @return - the agents name.
     * @throws XMPPErrorException 
     * @throws NoResponseException 
     * @throws NotConnectedException 
     */
    public String getName() throws NoResponseException, XMPPErrorException, NotConnectedException {
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setType(IQ.Type.GET);
        agentInfo.setTo(workgroupJID);
        agentInfo.setFrom(getUser());
        AgentInfo response = (AgentInfo) connection.createPacketCollectorAndSend(agentInfo).nextResultOrThrow();
        return response.getName();
    }

    /**
     * Changes the name of the agent in the server. The server may have this functionality
     * disabled for all the agents or for this agent in particular. If the agent is not
     * allowed to change his name then an exception will be thrown with a service_unavailable
     * error code.
     *
     * @param newName the new name of the agent.
     * @throws XMPPErrorException 
     * @throws NoResponseException 
     * @throws NotConnectedException 
     */
    public void setName(String newName) throws NoResponseException, XMPPErrorException, NotConnectedException {
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setType(IQ.Type.SET);
        agentInfo.setTo(workgroupJID);
        agentInfo.setFrom(getUser());
        agentInfo.setName(newName);
        connection.createPacketCollectorAndSend(agentInfo).nextResultOrThrow();
    }
}
