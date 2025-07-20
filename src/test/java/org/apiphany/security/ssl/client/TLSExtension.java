package org.apiphany.security.ssl.client;

import org.apiphany.security.tls.TLSObject;

public interface TLSExtension extends TLSObject {

	ExtensionType getType();

}
