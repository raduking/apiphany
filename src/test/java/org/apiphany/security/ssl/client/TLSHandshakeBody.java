package org.apiphany.security.ssl.client;

import org.apiphany.security.tls.TLSObject;

public interface TLSHandshakeBody extends TLSObject {

	HandshakeType getType();

}
