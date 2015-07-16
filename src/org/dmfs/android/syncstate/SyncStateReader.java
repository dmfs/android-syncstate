/*
 * Copyright (C) 2015 Marten Gajda <marten@dmfs.org>
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
 * 
 */

package org.dmfs.android.syncstate;

import org.dmfs.xmlobjects.ElementDescriptor;


/**
 * Interface of an object that can return values from a sync state.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public interface SyncStateReader
{
	/**
	 * Get the value for a given key from the sync state.
	 * 
	 * @param key
	 *            The {@link ElementDescriptor} of the vlaue to get.
	 * @return The value or <code>null</code> if no value exists for this key.
	 */
	public <V> V get(ElementDescriptor<V> key);
}
