package com.syf.blognew.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.syf.blognew.R;
import com.syf.blognew.activity.PublishActivity;
import com.syf.blognew.adapter.BlogAdapter;
import com.syf.blognew.pojo.*;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.api.NetCallBack;
import com.syf.blognew.api.NetClient;
import com.syf.blognew.pojo.req.CommentAddReq;
import com.syf.blognew.pojo.vo.BlogVO;
import com.syf.blognew.pojo.vo.CommentVO;
import com.syf.blognew.websocket.WebSocketManager;

import java.util.*;

import lombok.Getter;

public class HomeFragment extends Fragment {
    private List<BlogVO> blogList;
    private BlogAdapter blogAdapter;
    @Getter
    private ListView lvBlog;
    private static int current = 1;
    private static final int size = 10;

    public HomeFragment() {
    }

    public static HomeFragment newInstance(String label) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString("label", label);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context ctx=requireActivity();
        lvBlog = view.findViewById(R.id.lv_blog);

        SmartRefreshLayout refreshableView = view.findViewById(R.id.refreshable_view);

        blogList = new ArrayList<>();
        blogAdapter = new BlogAdapter(blogList, R.layout.item_blog, requireContext());

        lvBlog.setAdapter(blogAdapter);

        @SuppressLint("InflateParams") View emptyView = LayoutInflater.from(ctx).inflate(R.layout.empty_view, null);
        ((ViewGroup)lvBlog.getParent()).addView(emptyView,lvBlog.getLayoutParams());
        lvBlog.setEmptyView(emptyView);
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
            public void onDeleteBlog(int position) {
                deleteBlog(position);
            }

            @Override
            public void onEditBlog(int position) {
                Intent intent=new Intent(requireContext(), PublishActivity.class);
                intent.putExtra("blogId",blogList.get(position).getId());
                startActivity(intent);
            }

            @Override
            public void onSetPrivateBlog(int position) {
                setPrivateBlog(position);
            }

            @Override
            public void onsetPublicBlog(int position) {
                setPublicBlog(position);
            }

            @Override
            public void onAddComment(int position, String content, int fromId, Integer toId) {
                addComment(position, content, fromId, toId);
            }
        });

        // 下拉刷新
        refreshableView.setOnRefreshListener(v -> {
            initBlog();
            v.finishRefresh(1000);
        });

        // 上拉加载
        refreshableView.setOnLoadMoreListener(v -> {
            loadMoreBlog();
            v.finishLoadMore(10);
        });
        //进入发布页面
        view.findViewById(R.id.btn_publish).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PublishActivity.class);
            startActivity(intent);
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        initBlog();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(blogList.isEmpty()){
            initBlog();
        }
    }

    private void initBlog() {
        Runnable th = () -> {
            current = 1;
            NetClient.get(ApiConstant.BLOG_PAGE + current + "," + size, new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    requireActivity().runOnUiThread(() -> ToastHandler.showToast(msg));
                }

                @Override
                public void onSuccess(String json) {
                    PageResult<BlogVO> pageResult= JSONObject.parseObject(json, new TypeReference<>() {
                    });
                    List<BlogVO> newData =pageResult.getRecords();

                    blogList.clear();
                    blogList.addAll(0, newData);
                    requireActivity().runOnUiThread(() -> blogAdapter.notifyDataSetChanged());
                }
            });
        };
        new Thread(th).start();
    }

    private void loadMoreBlog() {
        Runnable loadThread = () -> {
            current++;
            NetClient.get(ApiConstant.BLOG_PAGE + current + "," + size, new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {}

                @Override
                public void onSuccess(String json) {
                    PageResult<BlogVO> blogPageResult = JSONObject.parseObject(json, new TypeReference<>() {});
                    if(blogPageResult.getRecords().isEmpty()) {
                        requireActivity().runOnUiThread(()->ToastHandler.showToast("没有更多内容了"));
                        return;
                    }
                    blogList.addAll(blogPageResult.getRecords());
                    requireActivity().runOnUiThread(() -> blogAdapter.notifyDataSetChanged());
                }
            });
        };
        new Thread(loadThread).start();
    }

    private void addSupport(int position) {
        Runnable addSupportThread = () -> NetClient.get(ApiConstant.SUPPORT_ADD + blogList.get(position).getId(), new NetCallBack() {
            @Override
            public void onFailure(int code, String msg) {
                requireActivity().runOnUiThread(() -> ToastHandler.showToast(msg));
            }

            @Override
            public void onSuccess(String json) {
                requireActivity().runOnUiThread(() -> {
                    blogList.get(position).getSupportList().add(WebSocketManager.getInstance().getUser().getName());
                    blogList.get(position).setIsSupport(1);
                    blogAdapter.notifyDataSetChanged();
                });
            }
        });
        new Thread(addSupportThread).start();
    }

    private void deleteSupport(int position) {
        Runnable deleteSupport = () -> NetClient.delete(ApiConstant.SUPPORT_DELETE + blogList.get(position).getId(), new NetCallBack() {
            @Override
            public void onFailure(int code, String msg) {
                requireActivity().runOnUiThread(() -> ToastHandler.showToast("取消点赞失败"));
            }

            @Override
            public void onSuccess(String json) {
                requireActivity().runOnUiThread(() -> {
                    blogList.get(position).getSupportList().remove(WebSocketManager.getInstance().getUser().getName());
                    blogList.get(position).setIsSupport(0);
                    blogAdapter.notifyDataSetChanged();
                });
            }
        });
        new Thread(deleteSupport).start();
    }

    private void deleteBlog(int position) {
        Runnable deleteBlogThread = () -> NetClient.delete(ApiConstant.BLOG_DELETE + blogList.get(position).getId(), new NetCallBack() {
            @Override
            public void onFailure(int code, String msg) {
                requireActivity().runOnUiThread(() -> ToastHandler.showToast("删除失败"));
            }

            @Override
            public void onSuccess(String json) {
                requireActivity().runOnUiThread(() -> {
                    ToastHandler.showToast("删除成功");
                    blogList.remove(position);
                    blogAdapter.notifyDataSetChanged();
                });
            }
        });
        new Thread(deleteBlogThread).start();
    }

    private void setPrivateBlog(int position) {
        Runnable thread = () -> NetClient.get(ApiConstant.BLOG_PRIVATE + blogList.get(position).getId(), new NetCallBack() {
            @Override
            public void onFailure(int code, String msg) {
                requireActivity().runOnUiThread(() -> ToastHandler.showToast("设置私密失败"));
            }

            @Override
            public void onSuccess(String json) {
                requireActivity().runOnUiThread(() -> {
                    ToastHandler.showToast("博客已私密");
                    blogList.get(position).setIsPrivate(1);
                    blogAdapter.notifyDataSetChanged();
                });
            }
        });
        new Thread(thread).start();
    }

    private void setPublicBlog(int position) {
        Runnable thread = () -> NetClient.get(ApiConstant.BLOG_PUBLIC + blogList.get(position).getId(), new NetCallBack() {
            @Override
            public void onFailure(int code, String msg) {
                requireActivity().runOnUiThread(() -> ToastHandler.showToast("设置公开失败"));
            }

            @Override
            public void onSuccess(String json) {
                requireActivity().runOnUiThread(() -> {
                    ToastHandler.showToast("博客已公开");
                    blogList.get(position).setIsPrivate(0);
                    blogAdapter.notifyDataSetChanged();
                });
            }
        });
        new Thread(thread).start();
    }

    private void addComment(int position, String content, int fromUid, Integer toUid) {
        if (content.isEmpty()) {
            requireActivity().runOnUiThread(() -> ToastHandler.showToast("请输入评论内容"));
            return;
        }

        Runnable th=()->{
            CommentAddReq req = new CommentAddReq();
            req.setBlogId(blogList.get(position).getId());
            req.setContent(content);
            req.setFromUid(fromUid);
            req.setToUid(toUid);

            NetClient.post(ApiConstant.COMMENT_ADD, req, new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    requireActivity().runOnUiThread(() -> ToastHandler.showToast("评论失败"));
                }

                @Override
                public void onSuccess(String json) {
                    requireActivity().runOnUiThread(() -> {
                        CommentVO newComment = JSONObject.parseObject(json, CommentVO.class);
                        BlogVO currentBlog = blogList.get(position);
                        currentBlog.getCommentList().add(newComment);
                        blogAdapter.notifyDataSetChanged();
                    });
                }
            });
        };
        new Thread(th).start();
    }
}