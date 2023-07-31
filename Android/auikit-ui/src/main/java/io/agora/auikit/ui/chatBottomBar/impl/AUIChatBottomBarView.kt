package io.agora.auikit.ui.chatBottomBar.impl

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.DrawableRes
import androidx.annotation.GravityInt
import androidx.annotation.IdRes
import androidx.appcompat.widget.LinearLayoutCompat
import io.agora.auikit.model.AUIExpressionIcon
import io.agora.auikit.ui.R
import io.agora.auikit.ui.chatBottomBar.IAUIChatBottomBarView
import io.agora.auikit.ui.chatBottomBar.listener.AUIExpressionClickListener
import io.agora.auikit.ui.chatBottomBar.listener.AUIMenuItemClickListener
import io.agora.auikit.ui.chatBottomBar.listener.AUISoftKeyboardHeightChangeListener
import io.agora.auikit.ui.chatBottomBar.utils.KeyboardUtils
import io.agora.auikit.ui.databinding.AuiChatBottomBarLayoutBinding
import io.agora.auikit.utils.DeviceTools

class AUIChatBottomBarView : RelativeLayout,
    IAUIChatBottomBarView, AUIExpressionClickListener,
    AUISoftKeyboardHeightChangeListener {
    private val mViewBinding = AuiChatBottomBarLayoutBinding.inflate(LayoutInflater.from(context))
    private val inputManager:InputMethodManager
    private val itemModels = ArrayList<MenuItemModel>()
    private var listener: AUIMenuItemClickListener?=null
    //是否显示表情
    private var isShowEmoji = false
    private var softKeyHeight = 0
    private var appearanceId:Int=0
    private var mTagIconBg: Int = 0
    private var mMoreStatusBg: Int = 0
    private var mFaceIcon:Int = 0
    private var mKeyIcon:Int = 0
    private var mMenuWidth:Int = 0
    private var mMenuHeight:Int = 0
    private var mMenuMicOn:Int = 0
    private var mMenuMicOff:Int = 0
    private var emojiViewBg:Int = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        addView(mViewBinding.root)
        inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        val themeTa = context.obtainStyledAttributes(attrs, R.styleable.AUIChatBottomView, defStyleAttr, 0)
        appearanceId = themeTa.getResourceId(R.styleable.AUIChatBottomView_aui_chatBottomView_appearance, 0)
        themeTa.recycle()

        initMenu()
        initListener()
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
            mViewBinding.iconEmoji.setImageResource(mFaceIcon)
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
        registerMenuItem(drawableRes, itemId)?.let {
            appendMenuView(it)
        }
    }

    fun removeMenu(itemId: Int){
        unRegisterMenuItem(itemId)?.let {
            deleteMenuItemView(itemId)
        }
    }

    fun updateMenuGravity(@IdRes itemId: Int, @GravityInt gravity: Int){
        getMenuItem(itemId)?.let {
            if(it.gravity != gravity){
                it.gravity = gravity
                refreshMenuLayout()
            }
        }
    }

    private fun initMenu() {
        val typedArray = context.obtainStyledAttributes(appearanceId, R.styleable.AUIChatBottomView)
        mMenuWidth = typedArray.getDimensionPixelSize(R.styleable.AUIChatBottomView_aui_primary_menuWidth, DeviceTools.dp2px(context, 38f))
        mMenuHeight = typedArray.getDimensionPixelSize(R.styleable.AUIChatBottomView_aui_primary_menuHeight, DeviceTools.dp2px(context, 38f))
        mMenuMicOn = typedArray.getResourceId(R.styleable.AUIChatBottomView_aui_primary_menuMicOn, R.drawable.voice_icon_mic_on)
        mMenuMicOff = typedArray.getResourceId(R.styleable.AUIChatBottomView_aui_primary_menuMicOff, R.drawable.voice_icon_mic_off)
        mTagIconBg = typedArray.getResourceId(
            R.styleable.AUIChatBottomView_aui_primary_tag_bg,
            R.drawable.aui_chat_bottom_bar_item_bg_light
        )
        mMoreStatusBg = typedArray.getResourceId(
            R.styleable.AUIChatBottomView_aui_primary_more_status,
            R.drawable.aui_chat_bottom_bar_more_status_bg
        )
        mFaceIcon = typedArray.getResourceId(
            R.styleable.AUIChatBottomView_aui_primary_emoji_resource,
            R.drawable.voice_icon_face_light
        )
        mKeyIcon = typedArray.getResourceId(
            R.styleable.AUIChatBottomView_aui_primary_key_resource,
            R.drawable.voice_icon_key_light
        )
        emojiViewBg = typedArray.getResourceId(
            R.styleable.AUIChatBottomView_aui_primary_expression_background,
            R.color.voice_white_100
        )
        mViewBinding.inputSend.setText(R.string.voice_room_send_tip)
        mViewBinding.menuLayout.removeAllViews()
        mViewBinding.normalLayout.visibility = VISIBLE
        mViewBinding.inputIcon.visibility = VISIBLE
        mViewBinding.menuLayout.visibility = VISIBLE
        mViewBinding.iconEmoji.setImageResource(mFaceIcon)
        registerMenuItem(R.drawable.voice_icon_more, R.id.voice_extend_item_more)
        registerMenuItem(mMenuMicOn, R.id.voice_extend_item_mic)
        registerMenuItem(R.drawable.voice_icon_gift, R.id.voice_extend_item_gift)
        registerMenuItem(R.drawable.voice_icon_like, R.id.voice_extend_item_like)
        refreshMenuLayout()
    }

    private fun refreshMenuLayout() {
        mViewBinding.menuLayout.removeAllViews()
        val startItemModels = itemModels.filter { it.gravity == Gravity.START }
        val endItemModels = itemModels.filter { it.gravity == Gravity.END }

        for (itemModel in startItemModels) {
            appendMenuView(itemModel)
        }
        mViewBinding.menuLayout.addView(View(context), LinearLayoutCompat.LayoutParams(0, 1, 1.0f))
        for (itemModel in endItemModels) {
            appendMenuView(itemModel)
        }
    }

    private fun appendMenuView(itemModel: MenuItemModel) {
        val context = context
        val imageView = ImageView(context)
        val marginLayoutParams = LinearLayoutCompat.LayoutParams(mMenuWidth, mMenuHeight)
        marginLayoutParams.marginStart = DeviceTools.dp2px(context, 8f)
        imageView.setPadding(
            DeviceTools.dp2px(context, 5f),
            DeviceTools.dp2px(context, 7f),
            DeviceTools.dp2px(context, 5f),
            DeviceTools.dp2px(context, 7f)
        )
        imageView.setImageResource(itemModel.image)
        imageView.setBackgroundResource(mTagIconBg)
        imageView.id = itemModel.id
        if (itemModel.id == R.id.voice_extend_item_more) {
            val relativeLayout = RelativeLayout(context)
            relativeLayout.layoutParams =
                LayoutParams(
                    DeviceTools.dp2px(context, 48f),
                    DeviceTools.dp2px(context, 38f)
                )

            val status = ImageView(context)
            status.id = R.id.voice_extend_item_more_status
            status.setImageResource(mMoreStatusBg)
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
        } else {
            imageView.layoutParams = marginLayoutParams
            mViewBinding.menuLayout.addView(imageView)
        }
        imageView.setOnClickListener { v ->
            listener?.onChatExtendMenuItemClick(v.id, v)
        }
    }

    private fun deleteMenuItemView(itemId: Int){
        val itemView = mViewBinding.menuLayout.findViewById<View>(itemId)
        if(itemView != null){
            if (itemId == R.id.voice_extend_item_more) {
                mViewBinding.menuLayout.removeView(itemView.parent as View)
            }else{
                mViewBinding.menuLayout.removeView(itemView)
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
            Log.e("apex","softShowing $softKeyHeight")
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
            setViewLayoutParams(
                mViewBinding.expressionView, ViewGroup.LayoutParams.MATCH_PARENT, DeviceTools.dp2px(
                    context, 55f
                )
            )
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
    private fun registerMenuItem(drawableRes: Int, itemId: Int, gravity: Int = Gravity.END): MenuItemModel? {
        var item = itemModels.find { it.id == itemId }
        if (item == null) {
            item = MenuItemModel(
                drawableRes,
                itemId,
                gravity
            )
            itemModels.add(item)
            return item;
        }
        return null
    }

    private fun unRegisterMenuItem(itemId: Int): MenuItemModel? {
        val item = itemModels.find { it.id == itemId }
        if(item != null){
            itemModels.remove(item)
            return item
        }
        return null
    }

    private fun getMenuItem(itemId: Int): MenuItemModel?{
        return itemModels.find { it.id == itemId }
    }

    override fun setEnableMic(isEnable: Boolean) {
        post {
            val mic: ImageView =
                mViewBinding.menuLayout.findViewById<ImageView>(R.id.voice_extend_item_mic)
            if (!isEnable) {
                mic.setImageResource(mMenuMicOn)
            } else {
                mic.setImageResource(mMenuMicOff)
            }
        }
    }

    override fun setShowMic(isShow: Boolean) {
        post {
            val mic: ImageView =
                mViewBinding.menuLayout.findViewById<ImageView>(R.id.voice_extend_item_mic)
            if (isShow) {
                mic.visibility = VISIBLE
            } else {
                mic.visibility = GONE
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
            mViewBinding.iconEmoji.setImageResource(mKeyIcon)
            mViewBinding.expressionView.visibility = VISIBLE
            hideKeyboard()
        } else {
            mViewBinding.iconEmoji.setImageResource(mFaceIcon)
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
                DeviceTools.dp2px(context, 55f)
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

    data class MenuItemModel(
        @DrawableRes val image: Int,
        @IdRes val id: Int,
        @GravityInt var gravity: Int
    )

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
                lp.height = DeviceTools.dp2px(context, 55f)
                showNormalLayout()
            }
        }
        mViewBinding.vKeyboardBg.layoutParams = lp
    }

    override fun setSoftKeyListener(){
        listener?.setSoftKeyBoardHeightChangedListener(this)
    }

}