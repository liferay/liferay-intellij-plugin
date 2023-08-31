/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ide.idea.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Terry Jia
 */
public class StringBufferOutputStream extends OutputStream {

	public StringBufferOutputStream() {
	}

	public void clear() {
		buffer.delete(0, buffer.length());
	}

	public String toString() {
		return buffer.toString();
	}

	/**
	 * @see OutputStream#write(int)
	 */
	public void write(int write) throws IOException {
		buffer.append((char)write);
	}

	protected StringBuffer buffer = new StringBuffer();

}