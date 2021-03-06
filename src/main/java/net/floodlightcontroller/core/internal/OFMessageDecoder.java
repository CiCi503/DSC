/**
*    Copyright 2011, Big Switch Networks, Inc.
*    Originally created by David Erickson, Stanford University
*
*    Licensed under the Apache License, Version 2.0 (the "License"); you may
*    not use this file except in compliance with the License. You may obtain
*    a copy of the License at
*
*         http://www.apache.org/licenses/LICENSE-2.0
*
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
*    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
*    License for the specific language governing permissions and limitations
*    under the License.
**/

package net.floodlightcontroller.core.internal;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFMessageReader;
import org.projectfloodlight.openflow.protocol.OFVersion;

/**
 * Decode an openflow message from a channel, for use in a netty pipeline.
 *	解码一个OF报文
 * @author readams
 */
public class OFMessageDecoder extends FrameDecoder {

    private OFMessageReader<OFMessage> reader;

    public OFMessageDecoder() {
        reader = OFFactories.getGenericReader();
    }

    public OFMessageDecoder(OFVersion version) {
        setVersion(version);
    }

    public void setVersion(OFVersion version) {
        OFFactory factory = OFFactories.getFactory(version);
        this.reader = factory.getReader();
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel,
                            ChannelBuffer buffer) throws Exception {
        if (!channel.isConnected()) {
            // In testing, I see decode being called AFTER decode last.
            // This check avoids that from reading corrupted frames
            return null;
        }

        List<OFMessage> messageList = new ArrayList<OFMessage>();
        for (;;) {
            OFMessage message = reader.readFrom(buffer);
            if (message == null)
                break;
            messageList.add(message);
        }
        return messageList.isEmpty() ? null : messageList;
    }

    @Override
    protected Object decodeLast(ChannelHandlerContext ctx, Channel channel,
                            ChannelBuffer buffer) throws Exception {
        // This is not strictly needed at this time. It is used to detect
        // connection reset detection from netty (for debug)
        return null;
    }
}
