LL.bindClass("android.R");
LL.bindClass("java.util.ArrayList");
LL.bindClass("android.view.ViewTreeObserver");
LL.bindClass("android.widget.Button");
LL.bindClass("android.view.View");
LL.bindClass("android.os.Build");

var sourceItem = LL.getEvent().getItem();
var context = LL.getContext();
var pkg = context.getPackageName();
var rsrc = context.getResources();
var id = rsrc.getIdentifier("bubble_content", "id",  pkg);
var menu =context.getWindow().getDecorView().findViewById(id);
var menuRoot = menu.getParent();
var version = Build.VERSION.SDK_INT;

var obs=menuRoot.getViewTreeObserver();
var l=new ViewTreeObserver.OnGlobalLayoutListener(){
    onGlobalLayout:function(){
        var list=menu;
        var first=list.getChildAt(0);
        list.removeAllViews();
        var v = !(typeof invisible === 'undefined' || invisible);
        add(v?"Hide Hidden":"Show Hidden",function(){
            LL.runAction(EventHandler.LAUNCHER_MENU);
            invisible = v;
            LL.runScript("AppDrawer_main",v?"":"showHidden");
        },first,list);
        if(sourceItem!=null){
            v = !JSON.parse(sourceItem.getTag("hide"));
            add(v?"Hide":"Unhide",function(){
                LL.runAction(EventHandler.LAUNCHER_MENU);
                if(v)LL.getEvent().getContainer().setItemZIndex(sourceItem.getId(),0);
                sourceItem.setVisibility(!v);
                sourceItem.setTag("hide",v);
                LL.runScript("AppDrawer_main","");
            },first,list);
        }
        add("Reload",function(){
            LL.runAction(EventHandler.LAUNCHER_MENU);
            LL.runScript("AppDrawer_main","reload");
        },first,list);
        add("Settings",function(){
            LL.runAction(EventHandler.LAUNCHER_MENU);
            LL.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.faendir.lightning_launcher.appdrawer"));
        },first,list)
        add("Lightning Menu",function(){
            LL.runAction(EventHandler.LAUNCHER_MENU);
            if(sourceItem==null) LL.runAction(EventHandler.LAUNCHER_MENU)
            else LL.runAction(EventHandler.ITEM_MENU,sourceItem,null);
        },first,list);
        obs.removeOnGlobalLayoutListener(l);
        return true;
    }
};﻿

obs.addOnGlobalLayoutListener(l);
LL.runAction(EventHandler.LAUNCHER_MENU);

function add(text,onClickFunction,first,list){
    var t=new Button(LL.getContext());
    if(version >= 16) t.setBackground(first.getBackground().mutate().getConstantState().newDrawable());
    else t.setBackgroundDrawable(first.getBackground().mutate().newDrawable());
    t.setTypeface(first.getTypeface());
    if(version >= 14) t.setAllCaps(false);
    t.setTextSize(0,first.getTextSize());
    if(version >= 21) t.setFontFeatureSettings(first.getFontFeatureSettings());
    t.setGravity(first.getGravity());
    t.setText(text);
    t.setOnClickListener(new View.OnClickListener(){
    onClick:onClickFunction
    });
    list.addView(t);
}