package com.su.mediabox.view.component.bannerview.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.su.mediabox.App
import com.su.mediabox.PluginManager.process
import com.su.mediabox.R
import com.su.mediabox.util.AnimeCover6ViewHolder
import com.su.mediabox.util.Util.getResColor
import com.su.mediabox.util.coil.CoilUtil.loadImage
import com.su.mediabox.util.showToast
import com.su.mediabox.util.ViewHolderUtil
import com.su.mediabox.util.gone
import com.su.mediabox.util.visible
import com.su.mediabox.plugin.standard.been.BaseBean
import com.su.mediabox.plugin.standard.been.AnimeCoverBean

/**
 * Created by Sky_D on 2021-02-08.
 */
class MyCycleBannerAdapter(
    private val activity: Activity,
    private val dataList: List<BaseBean>
) : CycleBannerAdapter() {
    override fun getItemType(position: Int): Int =
        ViewHolderUtil.getItemViewType(dataList[position])

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val viewHolder = ViewHolderUtil.getViewHolder(parent, viewType)
        //vp2的item必须是MATCH_PARENT的
        val layoutParams = viewHolder.itemView.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        viewHolder.itemView.layoutParams = layoutParams
        return viewHolder
    }

    override fun onBind(holder: RecyclerView.ViewHolder, position: Int) {
        val item = dataList[position]

        when (holder) {
            is AnimeCover6ViewHolder -> {
                if (item is AnimeCoverBean) {
                    holder.tvAnimeCover6Night.setBackgroundColor(activity.getResColor(R.color.transparent_skin))
                    holder.ivAnimeCover6Cover.loadImage(
                        item.cover?.url ?: "",
                        referer = item.cover?.referer
                    )
                    holder.tvAnimeCover6Title.text = item.title
                    holder.tvAnimeCover6Episode.text = item.episodeClickable?.title
                    if (item.describe.isNullOrEmpty()) {
                        holder.tvAnimeCover6Describe.gone()
                    } else {
                        holder.tvAnimeCover6Describe.visible()
                        holder.tvAnimeCover6Describe.text = item.describe
                    }
                    holder.itemView.setOnClickListener {
                        process(item.actionUrl)
                    }
                }
            }
            else -> {
                holder.itemView.visibility = View.GONE
                (App.context.resources.getString(R.string.unknown_view_holder) + position).showToast()
            }
        }
    }

    override fun getCount(): Int = dataList.size
}