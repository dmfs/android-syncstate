package org.dmfs.android.syncstate;

import android.accounts.Account;
import android.content.Context;
import android.os.RemoteException;

import org.dmfs.xmlobjects.ElementDescriptor;
import org.dmfs.xmlobjects.QualifiedName;
import org.dmfs.xmlobjects.XmlContext;
import org.dmfs.xmlobjects.builder.StringObjectBuilder;
import org.junit.Test;

import java.io.IOException;

import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


/**
 * Test {@link ContactsSyncState}.
 */
public class ContactsSyncStateTest
{

    /**
     * Test descriptor that we add to the sync state.
     */
    private final static ElementDescriptor<String> ELEMENT1 = ElementDescriptor.register(QualifiedName.get("http://dmfs.org/ns/1", "element1"),
            StringObjectBuilder.INSTANCE);

    /**
     * Another Test descriptor that we add to the sync state.
     */
    private final static ElementDescriptor<String> ELEMENT2 = ElementDescriptor.register(QualifiedName.get("http://dmfs.org/ns/2", "element2"),
            StringObjectBuilder.INSTANCE);

    /**
     * An {@link XmlContext}.
     */
    private final static XmlContext CONTEXT = new XmlContext();

    /**
     * Test descriptor that we add to the sync state. This element is not in the default context.
     */
    private final static ElementDescriptor<String> CONTEXT_ELEMENT1 = ElementDescriptor.register(QualifiedName.get("http://dmfs.org/ns/1", "context_element1"),
            StringObjectBuilder.INSTANCE, CONTEXT);

    /**
     * Another Test descriptor that we add to the sync state. This element is not in the default context.
     */
    private final static ElementDescriptor<String> CONTEXT_ELEMENT2 = ElementDescriptor.register(QualifiedName.get("http://dmfs.org/ns/2", "context_element2"),
            StringObjectBuilder.INSTANCE, CONTEXT);


    @Test
    public void testContactsSyncState() throws IOException, RemoteException
    {
        Account testAccount = new Account("test", "local" /* there is no "local account" for contacts */);

        // create a new ContactsSyncState for the test account
        SyncState s = new ContactsSyncState(getContext().getContentResolver(), testAccount);

        // the values must not exist yet
        assertNull(s.get(ELEMENT1));
        assertNull(s.get(ELEMENT2));

        // add two values
        s.set(ELEMENT1, "some string value");
        s.set(ELEMENT2, "some other string value");

        // check that the values are returned
        assertEquals("some string value", s.get(ELEMENT1));
        assertEquals("some other string value", s.get(ELEMENT2));

        // store the sync state
        s.store();

        // make sure that the values are still returned correctly
        assertEquals("some string value", s.get(ELEMENT1));
        assertEquals("some other string value", s.get(ELEMENT2));

        // create a new ContactsSyncState
        SyncState s2 = new ContactsSyncState(getContext().getContentResolver(), testAccount);

        // ensure it doesn't contain any values yet
        assertNull(s2.get(ELEMENT1));
        assertNull(s2.get(ELEMENT2));

        // load the sync state
        s2.load();

        // make sure that the values are still returned correctly
        assertEquals("some string value", s2.get(ELEMENT1));
        assertEquals("some other string value", s2.get(ELEMENT2));
    }


    @Test
    public void testContactsSyncStateWithContext() throws IOException, RemoteException
    {
        Account testAccount = new Account("test2", "local" /* there is no "local account" for contacts */);

        // create a new ContactsSyncState for the test account
        SyncState s = new ContactsSyncState(getContext().getContentResolver(), testAccount);

        // the values must not exist yet
        assertNull(s.get(CONTEXT_ELEMENT1));
        assertNull(s.get(CONTEXT_ELEMENT2));

        // add two values
        s.set(CONTEXT_ELEMENT1, "some string value");
        s.set(CONTEXT_ELEMENT2, "some other string value");

        // check that the values are returned
        assertEquals("some string value", s.get(CONTEXT_ELEMENT1));
        assertEquals("some other string value", s.get(CONTEXT_ELEMENT2));

        // store the sync state
        s.store(CONTEXT);

        // make sure that the values are still returned correctly
        assertEquals("some string value", s.get(CONTEXT_ELEMENT1));
        assertEquals("some other string value", s.get(CONTEXT_ELEMENT2));

        // create a new ContactsSyncState
        SyncState s2 = new ContactsSyncState(getContext().getContentResolver(), testAccount);

        // ensure it doesn't contain any values yet
        assertNull(s2.get(CONTEXT_ELEMENT1));
        assertNull(s2.get(CONTEXT_ELEMENT2));

        // load the sync state
        s2.load(CONTEXT);

        // make sure that the values are still returned correctly
        assertEquals("some string value", s2.get(CONTEXT_ELEMENT1));
        assertEquals("some other string value", s2.get(CONTEXT_ELEMENT2));
    }


    private Context getContext()
    {
        return InstrumentationRegistry.getInstrumentation().getContext();
    }
}
