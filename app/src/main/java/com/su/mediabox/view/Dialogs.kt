package com.su.mediabox.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.su.mediabox.R
import com.su.mediabox.databinding.DialogEpisodeBottomSheetBinding
import com.su.mediabox.plugin.AppRouteProcessor
import com.su.mediabox.plugin.PluginManager
import com.su.mediabox.pluginapi.UI.dp
import com.su.mediabox.pluginapi.v2.been.EpisodeData
import com.su.mediabox.pluginapi.v2.components.IVideoPlayComponent
import com.su.mediabox.util.createCoroutineScope
import com.su.mediabox.util.downloadanime.AnimeDownloadHelper
import com.su.mediabox.util.showToast
import com.su.mediabox.util.toMD5
import com.su.mediabox.view.adapter.type.*
import com.su.mediabox.view.viewcomponents.VideoPlayListViewHolder
import kotlinx.coroutines.*

class BottomSheetEpisodeViewHolder(
    parent: ViewGroup
) :
    VideoPlayListViewHolder.EpisodeViewHolder(parent) {

    private val itemColor =
        binding.root.context.resources.getColor(R.color.foreground_main_color_2_skin)

    init {
        binding.tvAnimeEpisode2.apply {
            setTextColor(itemColor)
        }
        binding.root.apply {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            setPadding(12.dp, 8.dp, 12.dp, 8.dp)
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                (layoutParams as ViewGroup.MarginLayoutParams).setMargins(8.dp, 4.dp, 8.dp, 4.dp)
            }
        }
    }

    override fun onBind(data: EpisodeData) {
        binding.tvAnimeEpisode2.text = data.name
    }
}

/**
 * 剧集抽屉Dialog
 */
fun episodeSheetDialog(
    context: Context,
    videName: String,
    episodeDataList: List<EpisodeData>
): BottomSheetDialog? {
    if (episodeDataList.isEmpty())
        return null

    var data = episodeDataList

    val bottomSheetDialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
    val coroutineScope by lazy { bottomSheetDialog.createCoroutineScope() }
    val component by lazy { PluginManager.acquireComponent(IVideoPlayComponent::class.java) }
    val binding = DialogEpisodeBottomSheetBinding.inflate(LayoutInflater.from(context))

    binding.episodeBottomSheetTitle.text =
        String.format(context.getString(R.string.episode_bottom_sheet_format, data.size))

    //关闭
    binding.episodeBottomSheetClose.setOnClickListener {
        bottomSheetDialog.dismiss()
    }

    //反转顺序
    binding.episodeBottomSheetOrder.setOnClickListener {
        coroutineScope.launch {
            data = data.asReversed()
            binding.episodeBottomSheetList.typeAdapter()
                .submitList(data) {
                    binding.episodeBottomSheetList.apply {
                        post { scrollToPosition(0) }
                    }
                }
        }
    }

    binding.episodeBottomSheetList.apply {
        grid(4).initTypeList(
            DataViewMapList()
                .registerDataViewMap<EpisodeData, BottomSheetEpisodeViewHolder>()
        ) {
            //长按缓存视频
            addViewHolderLongClickListener<BottomSheetEpisodeViewHolder> { pos ->
                bindingTypeAdapter.getData<EpisodeData>(pos)?.also {
                    "开始解析 ${it.name}，请勿关闭...".showToast()
                    coroutineScope.launch(Dispatchers.IO + SupervisorJob() + CoroutineExceptionHandler { _, e ->
                        coroutineScope.launch(Dispatchers.Main) {
                            e.printStackTrace()
                            "缓存错误:${e.message}".showToast()
                        }
                    }) {
                        component.getVideoPlayMedia(it.url).apply {
                            Log.d("下载", videoPlayUrl)
                            withContext(Dispatchers.Main) {
                                AnimeDownloadHelper.instance.downloadAnime(
                                    AppRouteProcessor.currentActivity!!.get()!! as AppCompatActivity,
                                    videoPlayUrl, videoPlayUrl.toMD5(), "$videName/$title"
                                )
                            }
                        }
                    }
                } ?: "剧集信息错误".showToast()
                true
            }
            submitList(data)
        }
    }
    bottomSheetDialog.setContentView(binding.root)

    return bottomSheetDialog
}