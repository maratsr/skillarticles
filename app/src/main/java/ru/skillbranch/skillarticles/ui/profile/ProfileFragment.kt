package ru.skillbranch.skillarticles.ui.profile


import android.graphics.drawable.Drawable
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions.circleCropTransform
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.fragment_profile.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.ui.base.BaseFragment
import ru.skillbranch.skillarticles.ui.base.Binding
import ru.skillbranch.skillarticles.ui.custom.ShimmerDrawable
import ru.skillbranch.skillarticles.ui.delegates.RenderProp
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.profile.ProfileState
import ru.skillbranch.skillarticles.viewmodels.profile.ProfileViewModel
import kotlin.properties.Delegates

class ProfileFragment : BaseFragment<ProfileViewModel>() {

    override val viewModel: ProfileViewModel by viewModels()
    override val layout: Int = R.layout.fragment_profile
    override val binding: Binding by lazy { ProfileBinding() }

    //var cornerRadius by Delegates.notNull<Int>()
    lateinit var avatarShimmer: ShimmerDrawable

    override fun setupViews() {
    }

    private fun updateAvatar(avatarUrl: String) {
        val avatarShimmer = ShimmerDrawable.Builder()
            .setBaseColor(root.getColor(R.color.color_gray_light))
            .setHighlightColor(requireContext().getColor(R.color.color_divider))
            .setShimmerWidth(resources.displayMetrics.widthPixels - root.dpToIntPx(168))
            .addShape(ShimmerDrawable.Shape.Round(root.dpToIntPx(168)))
            .build()
            .apply { start() }

        Glide.with(root)
        .load(avatarUrl)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean = false;

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                avatarShimmer.stop()
                return false
            }

        })
        .placeholder(avatarShimmer)
        .error(R.drawable.ic_avatar)
        .apply(circleCropTransform())
        .override(root.dpToIntPx(168))
        .into(iv_avatar)
    }

    inner class ProfileBinding() : Binding() {
        var avatar by RenderProp("") {updateAvatar(it) }
        var name by RenderProp("") { tv_name.text = it }
        var about by RenderProp("") { tv_about.text = it }
        var rating by RenderProp(0) { tv_rating.text = "Rating: $it" }
        var respect by RenderProp(0) { tv_respect.text = "Respect: $it" }

        override fun bind(data: IViewModelState) {
            data as ProfileState
            if (data.avatar != null){ avatar = data.avatar }
            if (data.name != null){ name = data.name }
            if (data.about != null){ about = data.about }
            rating = data.rating
            respect = data.respect
        }
    }
}
