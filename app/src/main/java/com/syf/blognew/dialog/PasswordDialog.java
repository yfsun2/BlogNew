package com.syf.blognew.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.syf.blognew.R;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;

public class PasswordDialog extends Dialog {

    private final String[] KEYBOARD = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "删除"};
    private List<String> passwordList = new ArrayList<>();
    private TextView[] pwdTextList = new TextView[6];
    private GridView gridKeyboard;
    @Setter
    private String money = "";

    // 密码输入完成回调
    public interface OnPasswordInputListener {
        void onInputFinish(String password);
    }

    private OnPasswordInputListener listener;

    public PasswordDialog(Context context) {
        super(context, R.style.BottomDialogStyle);
    }

    public void setOnPasswordInputListener(OnPasswordInputListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_password_input);

        // 初始化密码框
        pwdTextList[0] = findViewById(R.id.pwd_1);
        pwdTextList[1] = findViewById(R.id.pwd_2);
        pwdTextList[2] = findViewById(R.id.pwd_3);
        pwdTextList[3] = findViewById(R.id.pwd_4);
        pwdTextList[4] = findViewById(R.id.pwd_5);
        pwdTextList[5] = findViewById(R.id.pwd_6);

        gridKeyboard = findViewById(R.id.grid_keyboard);
        KeyboardAdapter adapter = new KeyboardAdapter();
        gridKeyboard.setAdapter(adapter);

        // 键盘点击
        adapter.setOnKeyClickListener(position -> {
            String key = KEYBOARD[position];
            if ("删除".equals(key)) {
                if (!passwordList.isEmpty()) {
                    passwordList.remove(passwordList.size() - 1);
                }
            } else if (!"".equals(key)) {
                if (passwordList.size() < 6) {
                    passwordList.add(key);
                }
            }
            updatePasswordView();

            // 输入满6位密码
            if (passwordList.size() == 6) {
                StringBuilder sb = new StringBuilder();
                for (String s : passwordList) {
                    sb.append(s);
                }
                if (listener != null) {
                    listener.onInputFinish(sb.toString());
                }
                dismiss();
            }
        });
    }

    // 更新密码显示
    private void updatePasswordView() {
        for (int i = 0; i < 6; i++) {
            if (i < passwordList.size()) {
                pwdTextList[i].setText("●");
            } else {
                pwdTextList[i].setText("");
            }
        }
    }

    // 键盘适配器
    private class KeyboardAdapter extends BaseAdapter {
        private OnKeyClickListener listener;

        public void setOnKeyClickListener(OnKeyClickListener listener) {
            this.listener = listener;
        }

        @Override
        public int getCount() {
            return KEYBOARD.length;
        }

        @Override
        public Object getItem(int position) {
            return KEYBOARD[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv;
            if (convertView == null) {
                tv = new TextView(parent.getContext());
                tv.setLayoutParams(new GridView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 180));
                tv.setTextSize(24);
                tv.setGravity(android.view.Gravity.CENTER);
                tv.setBackgroundResource(R.drawable.bg_keyboard);
            } else {
                tv = (TextView) convertView;
            }

            String text = KEYBOARD[position];
            tv.setText(text);

            if ("".equals(text)) {
                tv.setEnabled(false);
            }

            tv.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onKeyClick(position);
                }
            });
            return tv;
        }
    }

    public interface OnKeyClickListener {
        void onKeyClick(int position);
    }
}