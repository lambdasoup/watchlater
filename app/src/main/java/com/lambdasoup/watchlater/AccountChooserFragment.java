/*
 * Copyright (c) 2015 - 2016
 *
 *  Maximilian Hille <mh@lambdasoup.com>
 * Juliane Lehmann <jl@lambdasoup.com>
 *
 * This file is part of Watch Later.
 *
 * Watch Later is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Watch Later is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Watch Later.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.lambdasoup.watchlater;

import android.accounts.Account;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AccountChooserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AccountChooserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountChooserFragment extends Fragment {
    private static final String ARG_ACCOUNTS = "com.lambdasoup.watchlater.ARG_ACCOUNTS";
    private static final String KEY_CHECKED_ITEM_POSITION = "com.lambdasoup.watchlater.checked_item_position";

    private Account[] accounts;

    private OnFragmentInteractionListener listener;


    public AccountChooserFragment() {
        // Required empty public constructor
    }

    public static AccountChooserFragment newInstance(Account[] accounts) {
        AccountChooserFragment fragment = new AccountChooserFragment();
        Bundle args = new Bundle();
        args.putParcelableArray(ARG_ACCOUNTS, accounts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            accounts = (Account[]) getArguments().getParcelableArray(ARG_ACCOUNTS);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        View view = getView();
        if (view != null) {
            outState.putInt(KEY_CHECKED_ITEM_POSITION, ((ListView) view.findViewById(R.id.account_list)).getCheckedItemPosition());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View accountChooserView = inflater.inflate(R.layout.fragment_account_chooser, container, false);

        final ListView listView = (ListView) accountChooserView.findViewById(R.id.account_list);

        final ArrayAdapter<Account> adapter = new ArrayAdapter<Account>(getActivity(), R.layout.item_account, accounts) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView accountName;
                if (convertView != null) {
                    accountName = (TextView) convertView;
                } else {
                    accountName = (TextView) inflater.inflate(R.layout.item_account, parent, false);
                }
                accountName.setText(getItem(position).name);
                return accountName;
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(
                (parent, view, position, id) -> accountChooserView.findViewById(R.id.button_add_with_selected_account).setEnabled(true)
        );

        if (savedInstanceState != null) {
            int pos = savedInstanceState.getInt(KEY_CHECKED_ITEM_POSITION);
            if (pos != ListView.INVALID_POSITION) {
                listView.setItemChecked(pos, true);
                listView.setSelection(pos);

                accountChooserView.findViewById(R.id.button_add_with_selected_account).setEnabled(true);
            }
        }

        accountChooserView.findViewById(R.id.button_add_with_selected_account).setOnClickListener(v -> onMultiAccountDone());
        return accountChooserView;
    }

    private void onMultiAccountDone() {
        View view = getView();
        if (view == null) {
            return;
        }
        ListView accountsList = (ListView) view.findViewById(R.id.account_list);
        Account account = (Account) accountsList.getAdapter().getItem(accountsList.getCheckedItemPosition());

        if (((CheckBox) view.findViewById(R.id.checkbox_always_use_selected_account)).isChecked()) {
            listener.onSetDefaultAccount(account);

        }
        listener.onAccountChosen(account);
    }

    // need to keep this for compatibility with API level < 23
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnFragmentInteractionListener {
        void onAccountChosen(Account account);

        void onSetDefaultAccount(Account account);
    }
}
