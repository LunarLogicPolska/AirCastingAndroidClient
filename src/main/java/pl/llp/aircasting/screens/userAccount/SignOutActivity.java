/**
    AirCasting - Share your Air!
    Copyright (C) 2011-2012 HabitatMap, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    You can contact the authors by email at <info@habitatmap.org>
*/
package pl.llp.aircasting.screens.userAccount;

import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.common.ApplicationState;
import pl.llp.aircasting.screens.common.ToastHelper;
import pl.llp.aircasting.screens.common.base.DialogActivity;
import pl.llp.aircasting.screens.common.base.SimpleProgressTask;
import pl.llp.aircasting.screens.common.helpers.SettingsHelper;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionSensorManager;
import pl.llp.aircasting.storage.repository.SessionRepository;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.inject.Inject;

import roboguice.inject.InjectView;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/23/11
 * Time: 5:31 PM
 */
public class SignOutActivity extends DialogActivity implements View.OnClickListener {
    @Inject SettingsHelper settingsHelper;
    @Inject SessionRepository sessionRepository;
    @Inject CurrentSessionSensorManager mCurrentSessionSensorManager;
    @Inject ApplicationState mState;

    @InjectView(R.id.sign_out) Button signOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sign_out);
        initDialogToolbar("Sign Out");

        signOut.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_out:
                signOutOrFinishSession();
                break;
        }
    }

    private void signOutOrFinishSession() {
        //noinspection unchecked
        new SimpleProgressTask<Void, Void, Void>(this) {
            @Override
            protected Void doInBackground(Void... voids) {
                if (mCurrentSessionSensorManager.anySensorConnected()) {
                    if (mState.recording.isRecording()) {
                        showToast();
                    } else {
                        mCurrentSessionSensorManager.disconnectAll();
                        signOut();
                    }
                } else {
                    signOut();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                finish();
            }
        }.execute();
    }

    private void signOut() {
        settingsHelper.removeCredentials();
        sessionRepository.deleteUploaded();
        sessionRepository.deleteLocationless();
    }

    private void showToast() {
        final Activity activity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastHelper.show(activity, R.string.stop_recording_first, Toast.LENGTH_LONG);
            }
        });
    }
}
