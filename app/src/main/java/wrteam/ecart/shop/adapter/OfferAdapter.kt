package wrteam.ecart.shop.adapter

import android.app.Activity
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.VideoView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import wrteam.ecart.shop.R
import wrteam.ecart.shop.model.OfferImages

class OfferAdapter(val activity: Activity, private val offerList: ArrayList<OfferImages>) :
    RecyclerView.Adapter<OfferAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.offer_lyt, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (offerList[position].image != "") {
            holder.lytOfferImage.visibility = View.VISIBLE
            val path = offerList[position].image
            if (offerList[position].type == "image") {
                Picasso.get()
                    .load(path)
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.offer_placeholder)
                    .error(R.drawable.offer_placeholder)
                    .into(holder.offerImage)
                holder.offerImage.visibility = View.VISIBLE
            } else if (offerList[position].type == "video") {
                viewHolder = holder
                holder.lytVideo.visibility = View.VISIBLE
                holder.videoView.setVideoURI(Uri.parse(path))
                // starts the video
                holder.imgPlay.setOnClickListener { 
                    if (holder.imgPlay.tag == "play") {
                        holder.imgPlay.setImageDrawable(
                            ContextCompat.getDrawable(
                                activity, R.drawable.ic_pause
                            )
                        )
                        holder.imgPlay.tag = "pause"
                        holder.lytPlay.visibility = View.GONE
                        Handler(Looper.getMainLooper()).postDelayed({
                            holder.lytPlay.visibility = View.GONE
                        }, 3000)
                        holder.videoView.start()
                    } else {
                        holder.imgPlay.setImageDrawable(
                            ContextCompat.getDrawable(
                                activity, R.drawable.ic_play
                            )
                        )
                        holder.imgPlay.tag = "play"
                        holder.videoView.pause()
                    }
                }
                holder.videoView.setOnClickListener { 
                    if (holder.lytPlay.visibility == View.GONE) {
                        holder.lytPlay.visibility = View.VISIBLE
                    } else {
                        holder.lytPlay.visibility = View.GONE
                    }
                }
                holder.videoView.setOnPreparedListener { mp: MediaPlayer ->
                    mediaPlayer = mp
                    holder.imgVolume.setOnClickListener { 
                        if (holder.imgVolume.tag == "soundOn") {
                            mp.setVolume(0f, 0f)
                            holder.imgVolume.setImageDrawable(
                                ContextCompat.getDrawable(
                                    activity, R.drawable.ic_volume_off
                                )
                            )
                            holder.imgVolume.tag = "soundOff"
                        } else {
                            mp.setVolume(1f, 1f)
                            holder.imgVolume.setImageDrawable(
                                ContextCompat.getDrawable(
                                    activity, R.drawable.ic_volume_on
                                )
                            )
                            holder.videoView.isSoundEffectsEnabled = true
                            holder.imgVolume.tag = "soundOn"
                        }
                    }
                }
            }
        } else {
            holder.lytOfferImage.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return offerList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val offerImage: ImageView = itemView.findViewById(R.id.offerImage)
        val imgPlay: ImageView = itemView.findViewById(R.id.imgPlay)
        val imgVolume: ImageView = itemView.findViewById(R.id.imgVolume)
        val videoView: VideoView = itemView.findViewById(R.id.videoView)
        val lytOfferImage: CardView = itemView.findViewById(R.id.lytOfferImage)
        val lytPlay: RelativeLayout = itemView.findViewById(R.id.lytPlay)
        val lytVideo: RelativeLayout = itemView.findViewById(R.id.lytVideo)

    }

    companion object {
        lateinit var mediaPlayer: MediaPlayer
        lateinit var viewHolder: ViewHolder
    }
}