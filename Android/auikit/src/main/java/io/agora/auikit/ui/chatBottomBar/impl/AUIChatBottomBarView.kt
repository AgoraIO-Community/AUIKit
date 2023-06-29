package io.agora.auikit.ui.chatBottomBar.impl

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.widget.LinearLayoutCompat
import io.agora.auikit.R
import io.agora.auikit.databinding.AuiChatBottomBarLayoutBinding
import io.agora.auikit.model.AUIExpressionIcon
import io.agora.auikit.ui.chatBottomBar.IAUIChatBottomBarView
import io.agora.auikit.ui.chatBottomBar.listener.AUIExpressionClickListener
import io.agora.auikit.ui.chatBottomBar.listener.AUIMenuItemClickListener
import io.agora.auikit.ui.chatBottomBar.listener.AUISoftKeyboardHeightChangeListener
import io.agora.auikit.ui.chatBottomBar.utils.KeyboardUtils
import io.agora.auikit.utils.DeviceTools

class AUIChatBottomBarView : RelativeLayout,
    IAUIChatBottomBarView, AUIExpressionClickListener,
    AUISoftKeyboardHeightChangeListener {
    private var activity: Context
    private val mViewBinding = AuiChatBottomBarLayoutBinding.inflate(LayoutInflater.from(context))
    private val inputManager:InputMethodManager
    private val itemModels = ArrayList<MenuItemModel>()
    private val itemMap: Map<Int, MenuItemModel?> = HashMap()
    private var listener: AUIMenuItemClickListener?=null
    //是否显示表情
    private var isShowEmoji = false
    private var softKeyHeight = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        activity = context
        addView(mViewBinding.root)
        inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        initListener()
        mViewBinding.inputSend.setText(R.string.voice_room_send_tip)
        initMenu()
    }


    private fun initListener(){
        mViewBinding.expressionView.setExpressionListener(this)
        mViewBinding.inputEditView.setOnEditorActionListener { v, actionId, event ->
            when (actionId and EditorInfo.IME_MASK_ACTION) {
                EditorInfo.IME_ACTION_DONE -> {
                    sendMessage()
                }
                EditorInfo.IME_ACTION_NEXT -> {}
            }
            false
        }

        mViewBinding.inputEditView.onFocusChangeListener =
            OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    if (!isShowEmoji) mViewBinding.inputView.visibility = GONE
                }
            }

        mViewBinding.inputIcon.setOnClickListener{
            mViewBinding.inputView.visibility = VISIBLE
            mViewBinding.inputEditView.requestFocus()
            showInputMethod(mViewBinding.inputEditView)
            mViewBinding.expressionView.visibility = GONE
            mViewBinding.vKeyboardBg.visibility = INVISIBLE
            mViewBinding.inputIcon.visibility = GONE
            mViewBinding.inputIcon.isEnabled = false
            mViewBinding.iconEmoji.setImageResource(R.drawable.voice_icon_face)
            isShowEmoji = false
        }

        mViewBinding.iconEmoji.setOnClickListener{
            isShowEmoji = !isShowEmoji
            softShowing(isShowEmoji)
            checkShowExpression(isShowEmoji)
        }

        mViewBinding.inputSend.setOnClickListener{
            Log.e("apex","inputSend")
            sendMessage()
        }
    }

    fun addMenu(drawableRes: Int, itemId: Int) {
        registerMenuItem(drawableRes, itemId)
        if (!itemMap.containsKey(itemId)) {
            activity.let {
                val imageView = ImageView(it)
                imageView.layoutParams =
                    LayoutParams(
                        DeviceTools.dp2px(it, 48f), DeviceTools.dp2px(
                            it, 48f
                        )
                    )
                imageView.setPadding(
                    DeviceTools.dp2px(it, 7f),
                    DeviceTools.dp2px(it, 7f),
                    DeviceTools.dp2px(it, 7f),
                    DeviceTools.dp2px(it, 7f)
                )
                imageView.setImageResource(drawableRes)
                imageView.setBackgroundResource(R.drawable.aui_chat_bottom_bar_item_icon)
                imageView.id = itemId
                imageView.setOnClickListener { v ->
                     listener?.onChatExtendMenuItemClick(v.id, v)
                }
                mViewBinding.menuLayout.addView(imageView)
            }
        }
    }

    fun initMenu() {
        mViewBinding.menuLayout.removeAllViews()
        mViewBinding.normalLayout.visibility = VISIBLE
        mViewBinding.inputIcon.visibility = VISIBLE
        mViewBinding.menuLayout.visibility = VISIBLE
        registerMenuItem(R.drawable.voice_icon_more, R.id.voice_extend_item_more)
        registerMenuItem(R.drawable.voice_icon_mic_on, R.id.voice_extend_item_mic)
        registerMenuItem(R.drawable.voice_icon_gift, R.id.voice_extend_item_gift)
        registerMenuItem(R.drawable.voice_icon_like, R.id.voice_extend_item_like)
        addView()
    }

    private fun addView() {
        for (itemModel in itemModels) {
            activity.let {
                val imageView = ImageView(it)
                val marginLayoutParams = LinearLayoutCompat.LayoutParams(
                    DeviceTools.dp2px(
                        it, 38f
                    ), DeviceTools.dp2px(it, 38f)
                )
                marginLayoutParams.marginStart = DeviceTools.dp2px(it, 8f)
                imageView.setPadding(
                    DeviceTools.dp2px(it, 5f),
                    DeviceTools.dp2px(it, 7f),
                    DeviceTools.dp2px(it, 5f),
                    DeviceTools.dp2px(it, 7f)
                )
                imageView.setImageResource(itemModel.image)
                imageView.setBackgroundResource(R.drawable.aui_chat_bottom_bar_item_icon)
                imageView.id = itemModel.id
                if (itemModel.id == R.id.voice_extend_item_more){
                    val relativeLayout = RelativeLayout(activity)
                    relativeLayout.layoutParams =
                        LayoutParams(
                            DeviceTools.dp2px(activity, 48f),
                            DeviceTools.dp2px(activity, 38f)
                        )

                    val status = ImageView(activity)
                    status.id = R.id.voice_extend_item_more_status
                    status.setImageResource(R.drawable.aui_chat_bottom_bar_more_status_bg)
                    status.visibility = GONE

                    val imgLayout = LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT
                    )
                    imgLayout.addRule(ALIGN_PARENT_TOP or ALIGN_PARENT_RIGHT)
                    imgLayout.setMargins(0, 15, 15, 0)
                    relativeLayout.addView(imageView, marginLayoutParams)
                    relativeLayout.addView(status, imgLayout)
                    mViewBinding.menuLayout.addView(relativeLayout)
                }else{
                    imageView.layoutParams = marginLayoutParams
                    mViewBinding.menuLayout.addView(imageView)
                }
                imageView.setOnClickListener { v ->
                    listener?.onChatExtendMenuItemClick(v.id, v)
                }
            }
        }
    }

    private fun sendMessage(){
        if (!TextUtils.isEmpty(mViewBinding.inputEditView.text)){
            listener?.onSendMessage(
                mViewBinding.inputEditView.text.toString().trim { it <= ' ' })
        }
        hideKeyboard()
        showNormalLayout()
    }

    override fun setMenuItemClickListener(listener: AUIMenuItemClickListener?) {
       this.listener = listener
    }

    private fun setViewLayoutParams(view: View, width: Int, height: Int) {
        val lp = view.layoutParams
        lp.width = width
        lp.height = height
        view.layoutParams = lp
    }

    private fun softShowing(isShowEmoji: Boolean) {
        if (isShowEmoji) {
            setViewLayoutParams(
                mViewBinding.expressionView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                softKeyHeight
            )
            setViewLayoutParams(
                mViewBinding.vKeyboardBg,
                ViewGroup.LayoutParams.MATCH_PARENT,
                softKeyHeight
            )
        } else {
            activity.let {
                DeviceTools.dp2px(
                    it, 55f
                )
            }.let {
                setViewLayoutParams(
                    mViewBinding.expressionView, ViewGroup.LayoutParams.MATCH_PARENT, it
                )
            }
        }
    }

    /**
     * register menu item
     *
     * @param drawableRes
     * background of item
     * @param itemId
     * id
     */
    private fun registerMenuItem(drawableRes: Int, itemId: Int) {
        if (!itemMap.containsKey(itemId)) {
            val item = MenuItemModel()
            item.image = drawableRes
            item.id = itemId
            itemModels.add(item)
        }
    }

    override fun setEnableMic(isEnable: Boolean) {
        post {
            val mic: ImageView =
                mViewBinding.menuLayout.findViewById<ImageView>(R.id.voice_extend_item_mic)
            if (!isEnable) {
                mic.setImageResource(R.drawable.voice_icon_mic_on)
            } else {
                mic.setImageResource(R.drawable.voice_icon_mic_off)
            }
        }
    }

    override fun setShowMoreStatus(isOwner: Boolean?, isShowHandStatus: Boolean) {
        post {
            val moreStatus: ImageView =
                mViewBinding.menuLayout.findViewById<ImageView>(R.id.voice_extend_item_more_status)
            if (isOwner == true) {
                if (isShowHandStatus) {
                    moreStatus.visibility = VISIBLE
                } else {
                    moreStatus.visibility = GONE
                }
            } else {
                moreStatus.visibility = GONE
            }
        }
    }

    private fun checkShowExpression(isShow: Boolean) {
        isShowEmoji = isShow
        if (isShowEmoji) {
            mViewBinding.iconEmoji.setImageResource(R.drawable.voice_icon_key)
            mViewBinding.expressionView.visibility = VISIBLE
            hideKeyboard()
        } else {
            mViewBinding.iconEmoji.setImageResource(R.drawable.voice_icon_face)
            mViewBinding.expressionView.visibility = INVISIBLE
            showInputMethod(mViewBinding.inputEditView)
        }
    }

     override fun hideKeyboard() {
        KeyboardUtils.hideKeyboard(this)
    }

    private fun showInputMethod(editText: EditText) {
        KeyboardUtils.showKeyboard(editText)
    }

    private fun hideExpressionView(isShowEx: Boolean) {
        if (isShowEx) {
            mViewBinding.expressionView.visibility = VISIBLE
        } else {
            mViewBinding.expressionView.visibility = GONE
            setViewLayoutParams(
                mViewBinding.vKeyboardBg,
                ViewGroup.LayoutParams.MATCH_PARENT,
                activity.let { DeviceTools.dp2px(it, 55f) }
            )
        }
    }

    private fun showNormalLayout(): Boolean {
        if (mViewBinding.inputIcon.visibility != VISIBLE) {
            mViewBinding.inputEditView.setText("")
            showInput()
            mViewBinding.normalLayout.visibility = VISIBLE
            mViewBinding.menuLayout.visibility = VISIBLE
            hideExpressionView(false)
            return true
        }
        return false
    }


    private fun showInput() {
        mViewBinding.inputView.visibility = GONE
        mViewBinding.inputIcon.visibility = VISIBLE
        mViewBinding.inputIcon.isEnabled = true
    }

    class MenuItemModel {
        var image = 0
        var id = 0
    }

    override fun onDeleteImageClicked() {
        if (!TextUtils.isEmpty(mViewBinding.inputEditView.text)) {
            val event =
                KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL)
            mViewBinding.inputEditView.dispatchKeyEvent(event)
        }
    }

    override fun onExpressionClicked(emojiIcon: AUIExpressionIcon?) {
        if (emojiIcon != null) {
            mViewBinding.inputEditView.append(AUIEmojiUtils.getSmiledText(context, emojiIcon.labelString))
        }
    }

    override fun onSoftKeyboardHeightChanged(isKeyboardShowed: Boolean,keyboardHeight:Int?) {
        val lp: ViewGroup.LayoutParams = mViewBinding.vKeyboardBg.layoutParams
        if (isKeyboardShowed) {
            if (keyboardHeight != null) {
                lp.height = keyboardHeight
                softKeyHeight = keyboardHeight
            }
        } else {
            if (!isShowEmoji) {
                lp.height = DeviceTools.dp2px(activity, 55f)
                showNormalLayout()
            }
        }
        mViewBinding.vKeyboardBg.layoutParams = lp
    }

    override fun setSoftKeyListener(){
        listener?.setSoftKeyBoardHeightChangedListener(this)
    }

}