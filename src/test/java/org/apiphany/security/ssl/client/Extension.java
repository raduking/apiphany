package org.apiphany.security.ssl.client;

public interface Extension extends Sizeable, BinaryRepresentable {

	ExtensionType getType();

}
