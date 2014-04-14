/**
 *
 * Copyright 2014 Andriy Tsykholyas
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
package org.jivesoftware.smackx.hoxt.provider;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.test.util.TestUtils;
import org.jivesoftware.smackx.hoxt.packet.Base64BinaryChunk;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;

import static org.junit.Assert.*;

/**
 * Tests correct parsing of 'chunk' elements in Message stanza.
 */
public class Base64BinaryChunkProviderTest {

    @Test
    public void isNonLatsChunkParsedCorrectly() throws Exception {
        String base64Text = "iVBORw0KGgoAAAANSUhEUgAAASwAAAGQCAYAA";
        String string = "<chunk xmlns='urn:xmpp:http' streamId='Stream0001'>" + base64Text + "</chunk>";

        Base64BinaryChunkProvider provider = new Base64BinaryChunkProvider();
        XmlPullParser parser = TestUtils.getParser(string, "chunk");

        PacketExtension extension = provider.parseExtension(parser);
        assertTrue(extension instanceof Base64BinaryChunk);

        Base64BinaryChunk chunk = (Base64BinaryChunk) extension;
        assertEquals("Stream0001", chunk.getStreamId());
        assertFalse(chunk.isLast());
        assertEquals(base64Text, chunk.getText());
    }

    @Test
    public void isLatsChunkParsedCorrectly() throws Exception {
        String base64Text = "2uPzi9u+tVWJd+e+y1AAAAABJRU5ErkJggg==";
        String string = "<chunk xmlns='urn:xmpp:http' streamId='Stream0001' last='true'>" + base64Text + "</chunk>";

        Base64BinaryChunkProvider provider = new Base64BinaryChunkProvider();
        XmlPullParser parser = TestUtils.getParser(string, "chunk");

        PacketExtension extension = provider.parseExtension(parser);
        assertTrue(extension instanceof Base64BinaryChunk);

        Base64BinaryChunk chunk = (Base64BinaryChunk) extension;
        assertEquals("Stream0001", chunk.getStreamId());
        assertTrue(chunk.isLast());
        assertEquals(base64Text, chunk.getText());
    }
}
