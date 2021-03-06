package com.lufficc.ishuhui.adapter.viewholder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lufficc.ishuhui.R;
import com.lufficc.ishuhui.activity.WebActivity;
import com.lufficc.ishuhui.adapter.ChapterListAdapter;
import com.lufficc.ishuhui.data.source.chapter.images.ChapterImagesRepository;
import com.lufficc.ishuhui.manager.ChapterListManager;
import com.lufficc.ishuhui.model.Chapter;
import com.lufficc.ishuhui.model.Comic;
import com.lufficc.ishuhui.service.DownloadManager;
import com.lufficc.ishuhui.service.DownloadService;
import com.lufficc.ishuhui.utils.JsonUtil;
import com.lufficc.ishuhui.utils.PtrUtil;
import com.lufficc.lightadapter.ViewHolderProvider;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lufficc on 2016/9/5.
 */

public class ChapterListViewHolderProvider extends ViewHolderProvider<Chapter, ChapterListViewHolderProvider.ViewHolder> {

    private ChapterListAdapter adapter;
    private Context context;
    private Comic comic;

    public ChapterListViewHolderProvider(Context context, ChapterListAdapter adapter, Comic comic) {
        this.context = context;
        this.adapter = adapter;
        this.comic = comic;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent) {
        return new ViewHolder(layoutInflater.inflate(R.layout.item_chapter, parent, false));
    }

    @Override
    public void onBindViewHolder(Chapter chapter, ViewHolder viewHolder, int position) {
        viewHolder.onBindData(chapter, position);
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.chapter_icon)
        ImageView chapter_icon;

        @BindView(R.id.chapter_name)
        TextView chapter_name;

        @BindView(R.id.chapter_date)
        TextView chapter_date;

        @BindView(R.id.chapter_number)
        TextView chapter_number;

        @BindView(R.id.chapter_view)
        Button downLoad;

        @BindView(R.id.progressBar)
        ProgressBar progressBar;


        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @SuppressLint("SetTextI18n")
        void onBindData(final Chapter data, final int position) {
            chapter_name.setText(data.Title);
            chapter_number.setText(data.ChapterNo + "话");
            chapter_date.setText(data.RefreshTimeStr);
            Glide.with(itemView.getContext())
                    .load(data.FrontCover)
                    .centerCrop()
                    .placeholder(R.color.gray)
                    .into(chapter_icon);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    buildDialog(data);
                    return true;
                }
            });
            if (ChapterImagesRepository.getInstance().has(data.Id)) {
                downLoad.setText("已下载");
                downLoad.setEnabled(false);
            } else {
                downLoad.setText("下载");
                downLoad.setEnabled(true);
                if (DownloadManager.getInstance().isChapterDownloading(data.Id)) {
                    downLoad.setVisibility(View.GONE);
                    downLoad.setOnClickListener(null);
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    downLoad.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    downLoad.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            download(data);
                        }
                    });
                }
            }
        }

        private void buildDialog(final Chapter chapter) {
            new AlertDialog
                    .Builder(context)
                    .setItems(new String[]{"下载", "在网页中查看"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    download(chapter);
                                    break;
                                case 1:
                                    ChapterListManager.instance().setChapters(adapter.getData(), getAdapterPosition());
                                    PtrUtil.getInstance().start()
                                            .put("book" + chapter.BookId, JsonUtil.getInstance().toJson(chapter))
                                            .put("book_chapter_" + chapter.BookId, chapter.Sort)
                                            .commit();
                                    WebActivity.showWebView(context, chapter);
                                    break;
                            }
                        }
                    })
                    .create()
                    .show();
        }
    }


    private void download(Chapter chapter) {
        DownloadService.startDownload(context, comic.Title, chapter);
        Toast.makeText(context, chapter.Title + "已加入下载队列", Toast.LENGTH_SHORT).show();
    }
}
