package com.syf.blognew.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson2.JSON;
import com.google.android.material.tabs.TabLayout;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.syf.blognew.R;
import com.syf.blognew.activity.IssueActivity;
import com.syf.blognew.adapter.AbstractAdapter;
import com.syf.blognew.adapter.BlogAdapter;
import com.syf.blognew.pojo.*;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.api.NetCallBack;
import com.syf.blognew.api.NetClient;
import com.syf.blognew.pojo.req.CommentAddReq;
import com.syf.blognew.pojo.vo.BlogVO;
import com.syf.blognew.pojo.vo.CommentVO;
import com.syf.blognew.view.IndexListView;
import com.syf.blognew.view.RefreshableView;
import com.syf.blognew.websocket.WebSocketManager;

import java.util.*;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class IndexFragment extends Fragment {
    private List<BlogVO> mData;
    private BlogAdapter blogAdapter;
    public ListView list_blog;
    private static int current = 1;
    private static final int size = 10;

    public IndexFragment() {
    }

    public static IndexFragment newInstance(String label) {
        IndexFragment fragment = new IndexFragment();
        Bundle args = new Bundle();
        args.putString("label", label);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // 👉 关键修复：所有 findViewById 放到 onCreateView 里
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_index, container, false);

        // 在这里找控件，保证不为 null
        list_blog = root.findViewById(R.id.list_blog);
        SmartRefreshLayout refreshableView = root.findViewById(R.id.refreshable_view);
        ImageButton issue_btn = root.findViewById(R.id.issue_btn);
        mData = new ArrayList<>();
        blogAdapter = new BlogAdapter(mData, R.layout.item_list_blog, requireContext());
        list_blog.setAdapter(blogAdapter);

        blogAdapter.setOnItemClickListener(new BlogAdapter.OnBlogItemListener() {
            @Override
            public void onAddSupport(int position) {
                addSupport(position);
            }

            @Override
            public void onDeleteSupport(int position) {
                deleteSupport(position);
            }

            @Override
            public void onDeleteBlog(int position, AlertDialog dialog) {
                deleteBlog(position, dialog);
            }

            @Override
            public void onAddComment(int position, String content, int fromId, Integer toId) {
                addComment(position, content, fromId, toId);
            }
        });

        // 下拉刷新
        refreshableView.setOnRefreshListener(v -> {
            initBlog();
            v.finishRefresh(100);
        });

        // 上拉加载
        refreshableView.setOnLoadMoreListener(v -> {
            loadMoreBlog();
            v.finishLoadMore(100);
        });

        issue_btn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), IssueActivity.class);
            startActivity(intent);
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        initBlog();
    }

    private void initBlog() {
        Runnable initThread = () -> {
            current = 1;
            NetClient.get(ApiConstant.BLOG_PAGE + current + "," + size, new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    requireActivity().runOnUiThread(() -> ToastHandler.showToast(msg));
                }

                @Override
                public void onSuccess(String json) {
                    List<BlogVO> newData = JSONObject.parseObject(json, new TypeReference<PageResult<BlogVO>>() {
                    }).getRecords();

                    mData.clear();
                    mData.addAll(0, newData);
                    requireActivity().runOnUiThread(() -> blogAdapter.notifyDataSetChanged());
                }
            });
        };
        new Thread(initThread).start();
    }

    private void loadMoreBlog() {
        Runnable loadThread = () -> {
            try {
                current++;
                NetClient.get(ApiConstant.BLOG_PAGE + current + "," + size, new NetCallBack() {
                    @Override
                    public void onFailure(int code, String msg) {}

                    @Override
                    public void onSuccess(String json) {
                        PageResult<BlogVO> blogPageResult = JSONObject.parseObject(json, new TypeReference<PageResult<BlogVO>>() {});
                        mData.addAll(blogPageResult.getRecords());
                        requireActivity().runOnUiThread(() -> blogAdapter.notifyDataSetChanged());
                    }
                });
            } catch (Exception e) {
                Log.e("IndexFragment", Objects.requireNonNull(e.getMessage()));
            }
        };
        new Thread(loadThread).start();
    }

    private void addSupport(int position) {
        Runnable addSupportThread = () -> {
            NetClient.get(ApiConstant.SUPPORT_ADD + mData.get(position).getId(), new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    requireActivity().runOnUiThread(() -> ToastHandler.showToast(msg));
                }

                @Override
                public void onSuccess(String json) {
                    requireActivity().runOnUiThread(() -> {
                        mData.get(position).getSupportList().add(WebSocketManager.getInstance().getUser().getName());
                        mData.get(position).setIsSupport(1);
                        blogAdapter.notifyDataSetChanged();
                    });
                }
            });
        };
        new Thread(addSupportThread).start();
    }

    private void deleteSupport(int position) {
        Runnable deleteSupport = () -> {
            NetClient.delete(ApiConstant.SUPPORT_DELETE + mData.get(position).getId(), new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    requireActivity().runOnUiThread(() -> ToastHandler.showToast("取消点赞失败"));
                }

                @Override
                public void onSuccess(String json) {
                    requireActivity().runOnUiThread(() -> {
                        mData.get(position).getSupportList().remove(WebSocketManager.getInstance().getUser().getName());
                        mData.get(position).setIsSupport(0);
                        blogAdapter.notifyDataSetChanged();
                    });
                }
            });
        };
        new Thread(deleteSupport).start();
    }

    private void deleteBlog(int position, AlertDialog dialog) {
        Runnable deleteBlogThread = () -> {
            NetClient.delete(ApiConstant.BLOG_DELETE + mData.get(position).getId(), new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    requireActivity().runOnUiThread(() -> ToastHandler.showToast("删除失败"));
                }

                @Override
                public void onSuccess(String json) {
                    requireActivity().runOnUiThread(() -> {
                        ToastHandler.showToast("删除成功");
                        dialog.dismiss();
                        mData.remove(position);
                        blogAdapter.notifyDataSetChanged();
                    });
                }
            });
        };
        new Thread(deleteBlogThread).start();
    }

    private void addComment(int position, String content, int fromId, Integer toId) {
        if (content.isEmpty()) {
            requireActivity().runOnUiThread(() -> ToastHandler.showToast("请输入评论内容"));
            return;
        }
        MyRunnable addThread = new MyRunnable() {
            int blogId;
            String content;
            int fromUid;
            Integer toUid;

            @Override
            public MyRunnable setParam(Object... param) {
                blogId = (int) param[0];
                content = String.valueOf(param[1]);
                fromUid = (int) param[2];
                toUid = param[3] == null ? null : (int) param[3];
                return this;
            }

            @Override
            public void run() {
                CommentAddReq req = new CommentAddReq();
                req.setBlogId(blogId);
                req.setContent(content);
                req.setFromUid(fromUid);
                req.setToUid(toUid);

                RequestBody body = RequestBody.create(JSON.toJSONString(req), MediaType.parse("application/json; charset=utf-8"));
                NetClient.post(ApiConstant.COMMENT_ADD, body, new NetCallBack() {
                    @Override
                    public void onFailure(int code, String msg) {
                        requireActivity().runOnUiThread(() -> ToastHandler.showToast("评论失败"));
                    }

                    @Override
                    public void onSuccess(String json) {
                        CommentVO newComment = JSONObject.parseObject(json, CommentVO.class);
                        requireActivity().runOnUiThread(() -> {
                            BlogVO currentBlog = mData.get(position);
                            currentBlog.getCommentList().add(newComment);
                            blogAdapter.notifyDataSetChanged();
                            ToastHandler.showToast("评论成功");
                        });
                    }
                });
            }
        };
        new Thread(addThread.setParam(mData.get(position).getId(), content, fromId, toId)).start();
    }

    public interface MyRunnableInt {
        MyRunnable setParam(Object... param);
    }

    public static abstract class MyRunnable extends Thread implements MyRunnableInt {
        @Override
        public abstract MyRunnable setParam(Object... param);
    }
}