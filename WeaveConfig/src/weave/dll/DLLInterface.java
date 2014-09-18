/*
    Weave (Web-based Analysis and Visualization Environment)
    Copyright (C) 2008-2014 University of Massachusetts Lowell

    This file is a part of Weave.

    Weave is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License, Version 3,
    as published by the Free Software Foundation.

    Weave is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Weave.  If not, see <http://www.gnu.org/licenses/>.
*/

package weave.dll;

public class DLLInterface 
{
	static {
		System.setProperty("java.library.path", ".");
		
		// Load 32 or 64 bit version of DLL depending on system arch
		System.loadLibrary("DLLInterface" + System.getProperty("sun.arch.data.model"));
	}
	public static native void refresh() throws UnsatisfiedLinkError;
	public static native void flashTaskbar(String windowTitle, boolean flash);
}