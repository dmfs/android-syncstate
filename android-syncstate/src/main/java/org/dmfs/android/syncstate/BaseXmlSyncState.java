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

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.SyncStateContract;

import org.dmfs.xmlobjects.ElementDescriptor;
import org.dmfs.xmlobjects.QualifiedName;
import org.dmfs.xmlobjects.XmlContext;
import org.dmfs.xmlobjects.builder.ElementMapObjectBuilder;
import org.dmfs.xmlobjects.pull.XmlObjectPull;
import org.dmfs.xmlobjects.pull.XmlObjectPullParserException;
import org.dmfs.xmlobjects.pull.XmlPath;
import org.dmfs.xmlobjects.serializer.SerializerContext;
import org.dmfs.xmlobjects.serializer.SerializerException;
import org.dmfs.xmlobjects.serializer.XmlObjectSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * The base implementation of a {@link SyncState} that stores all values in an XML object.
 */
public abstract class BaseXmlSyncState implements SyncState
{
    private final static String[] PROJECTION = { SyncStateContract.Columns.DATA };
    private final static String SELECTION = SyncStateContract.Columns.ACCOUNT_NAME + "=? and " + SyncStateContract.Columns.ACCOUNT_TYPE + "=?";

    /**
     * Empty {@link XmlPath}.
     */
    private final static XmlPath EMPTY_PATH = new XmlPath();

    /**
     * The descriptor of the root element.
     */
    private final static ElementDescriptor<Map<ElementDescriptor<?>, Object>> SYNCSTATE_DESCRIPTOR = ElementDescriptor.register(
            QualifiedName.get("http://dmfs.org/ns/syncstate", "syncstate"), ElementMapObjectBuilder.INSTANCE);

    /**
     * A {@link ContentResolver}.
     */
    private final ContentResolver mResolver;

    /**
     * The {@link Account} of this sync state.
     */
    private final Account mAccount;

    /**
     * The {@link Uri} of this sync state. This also determines the authority.
     */
    private final Uri mUri;

    /**
     * A Map that stores all key values pairs if this sync state object.
     */
    private final Map<ElementDescriptor<?>, Object> mStateMap = new HashMap<ElementDescriptor<?>, Object>(16);


    /**
     * Initializes a new {@link BaseXmlSyncState} for the given {@link Account} and {@link Uri}.
     *
     * @param resolver
     *         A {@link ContentResolver} instance.
     * @param account
     *         The {@link Account} of the sync state.
     * @param uri
     *         The {@link Uri} of the sync state table.
     */
    public BaseXmlSyncState(ContentResolver resolver, Account account, Uri uri)
    {
        mResolver = resolver;
        mAccount = account;
        mUri = uri;
    }


    @Override
    public void load() throws IOException
    {
        load(ElementDescriptor.DEFAULT_CONTEXT);
    }


    @Override
    public void load(XmlContext xmlContext) throws IOException
    {
        try
        {
            try (Cursor c = mResolver.query(mUri, PROJECTION, SELECTION, new String[] { mAccount.name, mAccount.type }, null))
            {
                if (c == null || !c.moveToFirst())
                {
                    // there is no syncstate yet.
                    return;
                }

                byte[] data = c.getBlob(0);

                if (data == null)
                {
                    // there is no syncstate yet.
                    return;
                }

                InputStream in = new ByteArrayInputStream(data);
                if (data.length > 2 && (data[0] == (byte) (GZIPInputStream.GZIP_MAGIC & 0xff)) && (data[1] == (byte) ((GZIPInputStream.GZIP_MAGIC >> 8) & 0xff)))
                {
                    // data looks gzip compressed, wrap input in a GZIPInputStream
                    in = new GZIPInputStream(in);
                }

                // get a pull parser
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(in, "UTF-8");
                XmlObjectPull mObjectPull = new XmlObjectPull(parser);
                mObjectPull.setContext(xmlContext);
                mObjectPull.pull(SYNCSTATE_DESCRIPTOR, mStateMap, EMPTY_PATH);
            }
        }
        catch (XmlPullParserException | XmlObjectPullParserException e)
        {
            // the constructor IOException(String, Throwable) is not available on Android 2.2 :-(
            throw (IOException) (new IOException("can't read syncstate").initCause(e));
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public <V> V set(ElementDescriptor<V> key, V value)
    {
        // we can safely cast, the <V> parameter will ensure type safety.
        return (V) mStateMap.put(key, value);
    }


    @SuppressWarnings("unchecked")
    @Override
    public <V> V get(ElementDescriptor<V> key)
    {
        // we can safely cast, the <V> parameter will ensure type safety.
        return (V) mStateMap.get(key);
    }


    @Override
    public void store() throws IOException
    {
        store(ElementDescriptor.DEFAULT_CONTEXT, true);
    }


    @Override
    public void store(XmlContext xmlContext) throws IOException
    {
        store(xmlContext, true);
    }


    /**
     * Persist the sync state using the given {@link XmlContext}. This method is meant for debugging purposes, since {@link #store()} and {@link
     * #store(XmlContext)} compress the data by default, making it harder to debug.
     *
     * @param xmlContext
     *         The {@link XmlContext} to use.
     * @param compress
     *         <code>true</code> to compress the sync state data, <code>false</code> to store it in plain text.
     *
     * @throws IOException
     */
    public void store(XmlContext xmlContext, boolean compress) throws IOException
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(mStateMap.size() * 100 /* assume at least 100 bytes per entry */);
        OutputStream out = compress ? new GZIPOutputStream(byteArrayOutputStream) : byteArrayOutputStream;

        try
        {
            SerializerContext context = new SerializerContext(xmlContext);
            XmlObjectSerializer serializer = new XmlObjectSerializer();
            for (ElementDescriptor<?> descriptor : mStateMap.keySet())
            {
                serializer.useNamespace(context, descriptor);
            }
            serializer.setOutput(context, out, "UTF-8");
            serializer.serialize(context, SYNCSTATE_DESCRIPTOR, mStateMap);
            out.flush();
            out.close();
            ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
            operations.add(SyncStateContract.Helpers.newSetOperation(mUri, mAccount, byteArrayOutputStream.toByteArray()));

            ContentProviderResult[] result = mResolver.applyBatch(mUri.getAuthority(), operations);
        }
        catch (SerializerException | RemoteException | OperationApplicationException e)
        {
            throw new IOException("can't persist syncstate", e);
        }
    }
}
