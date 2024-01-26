/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.util;

import org.dom4j.Element;

/**
 * @author Steve Ebersole
 */
@SuppressWarnings("WeakerAccess")
public class DomHelper {
	private DomHelper() {
	}

	public static String extractValue(Element element) {
		if ( element == null ) {
			return null;
		}

		final String value = element.getTextTrim();
		if ( value != null && value.isEmpty()) {
			return null;
		}

		return value;
	}
}
