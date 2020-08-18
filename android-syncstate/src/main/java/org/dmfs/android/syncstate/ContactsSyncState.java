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
import android.content.ContentResolver;
import android.provider.ContactsContract;


/**
 * Abstract class providing a high-level interface to {@link ContactsContract.SyncState}.
 */
public final class ContactsSyncState extends BaseXmlSyncState
{

    /**
     * Create a new {@link ContactsSyncState} for the given account.
     *
     * @param resolver
     *         A {@link ContentResolver} instance.
     * @param account
     *         The account of the sync state.
     */
    public ContactsSyncState(ContentResolver resolver, Account account)
    {
        super(resolver, account, ContactsContract.SyncState.CONTENT_URI.buildUpon().appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(ContactsContract.SyncState.ACCOUNT_TYPE, account.type)
                .appendQueryParameter(ContactsContract.SyncState.ACCOUNT_NAME, account.name).build());
    }

}
