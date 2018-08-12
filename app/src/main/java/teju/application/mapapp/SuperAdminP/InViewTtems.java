package teju.application.mapapp.SuperAdminP;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import teju.application.mapapp.R;

class User{
    String username;
    boolean selected=false;

    public User(String username, boolean selected) {
        this.username = username;
        this.selected = selected;
    }

    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
public class InViewTtems extends ArrayAdapter <User>{

    private List<User> userList;
    private Context context;
    public InViewTtems(List<User> userList,Context context) {
        super(context, R.layout.singlelistitem,userList);
        this.context=context;
        this.userList=userList;
    }
    private static class UserHolder{
        public TextView userName;
        public CheckBox checkbx;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v=convertView;

        try {
            UserHolder userHolder = new UserHolder();
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
                v = layoutInflater.inflate(R.layout.singlelistitem, null);

                userHolder.userName = (TextView) v.findViewById(R.id.name);
                userHolder.checkbx = (CheckBox) v.findViewById(R.id.checkbox);
                userHolder.checkbx.setOnCheckedChangeListener((Usersassign) context);
            } else {
                userHolder = (UserHolder) v.getTag();
            }


            User u = userList.get(position);
            userHolder.userName.setText(u.getUsername());
            userHolder.checkbx.setChecked(u.isSelected());
        }catch (Exception e){
            e.printStackTrace();
        }
        return v;
    }
}
