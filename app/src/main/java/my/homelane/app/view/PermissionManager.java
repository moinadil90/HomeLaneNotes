package my.homelane.app.view;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple class to make the runtime permission simpler
 * Created by moinadil on 23/04/18.
 */
public class PermissionManager {

    protected PermissionManager() {
        throw new AssertionError("utility class should not be instantiated");
    }

    /**
     * Handle all the runtime permission
     *
     * @param thisActivity          the activity that request the permission
     * @param permission            The permission to prompt to the user
     * @param rationale             the explanation on why we are requesting the permission
     * @param permissionRequestCode The request code that will be return in <a href="http://developer.android.com/reference/android/support/v4/app/ActivityCompat.OnRequestPermissionsResultCallback.html#onRequestPermissionsResult(int, java.lang.String[], int[])">onRequestPermissionsResult</a>
     * @return Status of this specific permission
     */
    public static PermissionStatus requestPermission(Activity thisActivity, String permission, String rationale, int permissionRequestCode) {
        PermissionStatus status = checkPermission(thisActivity, permission, rationale);
        if (PermissionStatus.REQUESTED.equals(status) || PermissionStatus.DENIED.equals(status)) {
            ActivityCompat.requestPermissions(thisActivity, new String[]{permission}, permissionRequestCode);
        }
        return status;
    }

    /**
     * Handle all the runtime permission
     *
     * @param thisActivity          the activity that request the permission
     * @param permission            The permission to prompt to the user
     * @param rationaleResId        The String resource id of the explanation on why we are requesting the permission
     * @param permissionRequestCode The request code that will be return in <a href="http://developer.android.com/reference/android/support/v4/app/ActivityCompat.OnRequestPermissionsResultCallback.html#onRequestPermissionsResult(int, java.lang.String[], int[])">onRequestPermissionsResult</a>
     * @return Status of this specific permission
     */

    public static PermissionStatus requestPermission(Activity thisActivity, String permission, int rationaleResId, int permissionRequestCode) {
        return requestPermission(thisActivity, permission, thisActivity.getString(rationaleResId), permissionRequestCode);
    }

    /**
     * Handle all the runtime permission
     *
     * @param fragment              the fragment that request the permission
     * @param permission            The permission to prompt to the user
     * @param rationale             the explanation on why we are requesting the permission
     * @param permissionRequestCode The request code that will be return in <a href="http://developer.android.com/reference/android/support/v4/app/Fragment.html#onRequestPermissionsResult(int, java.lang.String[], int[])">onRequestPermissionsResult</a>
     * @return Status of this specific permission
     */
    public static PermissionStatus requestPermission(Fragment fragment, String permission, String rationale, int permissionRequestCode) {
        // Here, thisActivity is the current activity
        Activity thisActivity = fragment.getActivity();
        if (thisActivity != null) {
            PermissionStatus status = checkPermission(thisActivity, permission, rationale);
            if (PermissionStatus.REQUESTED.equals(status) || PermissionStatus.DENIED.equals(status)) {
                fragment.requestPermissions(new String[]{permission}, permissionRequestCode);
            }
            return status;
        }
        return PermissionStatus.UNKNOWN;
    }

    /**
     * Handle all the runtime permission
     *
     * @param fragment              the fragment that request the permission
     * @param permissions           The group of permissions to prompt to the user
     * @param rationale             the explanation on why we are requesting the permission
     * @param permissionRequestCode The request code that will be return in <a href="http://developer.android.com/reference/android/support/v4/app/Fragment.html#onRequestPermissionsResult(int, java.lang.String[], int[])">onRequestPermissionsResult</a>
     * @return Status of this specific permission
     */
    public static PermissionStatus[] requestPermission(Fragment fragment, String[] permissions, String rationale, int permissionRequestCode) {
        // Here, thisActivity is the current activity
        Activity thisActivity = fragment.getActivity();

        PermissionStatus[] statuses = new PermissionStatus[permissions.length];

        List<String> permissionsToBeRequested = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            if (thisActivity != null) {
                PermissionStatus status = checkPermission(thisActivity, permission, rationale);
                if (PermissionStatus.REQUESTED.equals(status) || PermissionStatus.DENIED.equals(status)) {
                    permissionsToBeRequested.add(permission);
                }
                statuses[i] = status;
            } else {
                statuses[i] = PermissionStatus.UNKNOWN;
            }
        }

        if (permissionsToBeRequested.size() > 0) {
            fragment.requestPermissions(permissionsToBeRequested.toArray(new String[permissionsToBeRequested.size()]),
                    permissionRequestCode);
        }

        return statuses;
    }


    /**
     * Handle all the runtime permission
     *
     * @param fragment              the fragment that request the permission
     * @param permission            The permission to prompt to the user
     * @param rationaleResId        the explanation on why we are requesting the permission
     * @param permissionRequestCode The request code that will be return in <a href="http://developer.android.com/reference/android/support/v4/app/Fragment.html#onRequestPermissionsResult(int, java.lang.String[], int[])">onRequestPermissionsResult</a>
     * @return Status of this specific permission
     */
    public static PermissionStatus requestPermission(Fragment fragment, String permission, int rationaleResId, int permissionRequestCode) {
        return requestPermission(fragment, permission, fragment.getString(rationaleResId), permissionRequestCode);
    }

    /**
     * Generic function to check if the permission is set
     *
     * @param thisActivity the activity that request the permission
     * @param permission   The permission to prompt to the user
     * @param rationale    The String resource id of the explanation on why we are requesting the permission
     * @return Status of this specific permission
     */
    private static PermissionStatus checkPermission(Activity thisActivity, String permission, String rationale) {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(thisActivity, permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                    permission)) {
                //TODO refactor to a snackbar or an alert screen
                //Toast.makeText(thisActivity, rationale, Toast.LENGTH_LONG).show();
                return PermissionStatus.DENIED;

            }
            return PermissionStatus.REQUESTED;
        }
        return PermissionStatus.GRANTED;
    }

    public enum PermissionStatus {
        GRANTED, DENIED, REQUESTED, UNKNOWN
    }
}
