LL.bindClass("android.net.Uri");
LL.bindClass("android.graphics.BitmapFactory");
LL.bindClass("android.app.ProgressDialog");
LL.bindClass("java.lang.Runnable");
LL.bindClass("java.lang.Class");
LL.bindClass("dalvik.system.PathClassLoader");
var TAG = "appdrawer_lukas";

var e = LL.getEvent();
var c = e.getContainer();
var data = JSON.parse(c.getTag(TAG));
if(data == null)data = {};
if(data.db == null) data.db = [];
if(data.rows == null) data.rows = 0;
if(data.columns == null) data.columns =  5;
if(data.group == null) data.group = false;
var eventData = e.getData();
var showHidden = (eventData == "showHidden");
var initialize = (data.db.length == 0 || eventData == "reload");
var ctx = LL.getContext();
var pm = ctx.getPackageManager();
var progress = null;
var resolver;


var apk = pm.getApplicationInfo("com.faendir.lightning_launcher.appdrawer",0).sourceDir;
var clsLoader = new PathClassLoader(apk,PathClassLoader.getSystemClassLoader());
var cls = Class.forName("com.faendir.lightning_launcher.appdrawer.LightningTask",true,clsLoader);
var itemCls = Class.forName("com.faendir.lightning_launcher.appdrawer.LightningTask$Item",true,clsLoader);
task=cls.newInstance();
task.setPreExecute(new Runnable(){
    run:function(){
        if(initialize){
            progress = new ProgressDialog(LL.getContext());
            progress.setTitle("Initializing App Drawer")
            progress.setMessage("Please wait...");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setCancelable(false);
            progress.show();
            var i=c.getItems();
            for(var a = 0; a < i.length; a++) c.removeItem(i.getAt(a));
            data.db = [];
        }
    }
});
task.setPostExecute(new Runnable(){
    run:function(){
        sort();
        if(progress != null)progress.dismiss();
}});
task.setUpdate(new Runnable(){
    run:function(){
        var itemData = task.getItem();
        var i = itemData.getIntent();
        var app = itemData.getInfo();
        var bmp = itemData.getBitmap();
        var b = itemData.getPosition();
        var img = LL.createImage(bmp.getWidth(),bmp.getHeight());
        img.draw().drawBitmap(bmp,0,0,null);
        var item = c.addShortcut(app.loadLabel(pm),i,0,0);
        item.setName(item.getLabel());
        item.setTag("hide",false);
        item.setDefaultIcon(img);
        item.getProperties().edit().setEventHandler("i.longTap",EventHandler.RUN_SCRIPT,LL.getScriptByName("AppDrawer_menu").getId()).commit();
        data.db[b][0] = item.getId();
    }
});
task.setBackground(new Runnable(){
    run:function(){
        resolver = ctx.getContentResolver();
        loadValues();
        for(var a = 0; a < data.db.length; a++) data.db[a][2] = false;
        var intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        var apps = pm.queryIntentActivities(intent,0);
        for(var x = 0; x < apps.size(); x++){
            var app = apps.get(x);
            var name = app.activityInfo.name;
            var pkg = app.activityInfo.packageName;
            var b;
            for(b = 0; b < data.db.length && data.db[b][1] != name; b++);
            if(b == data.db.length) {
                var i = new Intent(intent);
                i.setClassName(pkg, name);
                var drawableCursor = resolver.query(Uri.parse("content://com.faendir.lightning_launcher.appdrawer.provider/icon"),null,i.getComponent().flattenToString(),null,null);
                var bmp;
                if(drawableCursor.moveToFirst()){
                    blob = drawableCursor.getBlob(0);
                    bmp = BitmapFactory.decodeByteArray(blob,0,blob.length);
                }
                else {
                    bmp = pm.getActivityIcon(i).getBitmap();
                }
                data.db[b] = [];
                data.db[b][1] = name;
                var item = itemCls.newInstance();
                item.setIntent(i);
                item.setInfo(app);
                item.setBitmap(bmp);
                item.setPosition(b);
                task.setItem(item);
                task.update();
            }
            data.db[b][2] = true;
        }
    }
});
task.execute();

function loadValues(){
    var rowCursor = resolver.query(Uri.parse("content://com.faendir.lightning_launcher.appdrawer.provider/rows"),null,null,null,null);
    if(rowCursor.moveToFirst()) data.rows = rowCursor.getInt(0);
    var columnCursor = resolver.query(Uri.parse("content://com.faendir.lightning_launcher.appdrawer.provider/columns"),null,null,null,null);
    if(columnCursor.moveToFirst()) data.columns = columnCursor.getInt(0);
    var groupCursor = resolver.query(Uri.parse("content://com.faendir.lightning_launcher.appdrawer.provider/groupItems"),null,null,null,null);
    if(groupCursor.moveToFirst()) data.group = groupCursor.getInt(0) == 1;
}

function sort(){
    var items=[];
    for(var a = 0; a < data.db.length; a++){
        if(data.db[a][0] == null || isNaN(data.db[a][0]))continue;
        var item=LL.getItemById(data.db[a][0]);
        if(item==null || item.getName().length == 1)continue;
        if(data.db[a][2]){
            if(showHidden||!JSON.parse(item.getTag("hide")))items.push(item);
            else item.setVisibility(false);
        }
        else{
           c.removeItem(item);
           data.db.splice(a,1);
           a--;
        }
    }
    c.setTag(TAG,JSON.stringify(data));
    items.sort(itemLabelSort);
    if(!data.group){
        for(var a = 0; a < items.length; a++){
            var item = items[a];
            item.setVisibility(true);
            var column = Math.floor(a/data.columns);
            var row = a%data.columns;
            if(data.rows==0){
                item.setCell(row,column,row+1,column+1);
            }
            else {
                var page = Math.floor(a/(data.rows*data.columns));
                item.setCell(row+data.columns*page,column-data.rows*page, row+data.columns*page+1,column-data.rows*page+1);
            }
        }
    }
    else{
        var width = c.getWidth();
        var headerHeight = 30;
        var cWidth = c.getCellWidth();
        var cHeight = c.getCellHeight();
        var offsetX = 0;
        var offsetY = 0;
        var start = '';
        for(var a = 0; a < items.length; a++){
            var item = items[a];
            item.setVisibility(true);
            if(start != item.getLabel().charAt(0).toUpperCase()){
                start = item.getLabel().charAt(0).toUpperCase();
                if(offsetX != 0) offsetY += cHeight;
                offsetX = 0;
                var header = c.getItemByName(start) || c.addShortcut(start,new Intent(),0,0);
                header.setName(start);
                var prop = header.getProperties().edit();
                prop.setBoolean("i.enabled",false);
                prop.setBoolean("i.onGrid",false);
                prop.setBoolean("s.iconVisibility",false);
                prop.getBox("i.box").setColor("c","nsf",0xffaaaaaa);
                prop.commit();
                header.setPosition(0,offsetY);
                header.setSize(width,headerHeight);
                offsetY += headerHeight;
            }
            item.getProperties().edit().setBoolean("i.onGrid",false).commit();
            item.setPosition(offsetX,offsetY);
            offsetX += cWidth;
            if(offsetX/cWidth >= data.columns){
                offsetX = 0;
                offsetY += cHeight;
            }
        }
    }
}

var itemLabelSort = function(x,y){
    var a = x.getLabel();
    var b = y.getLabel();
    var result = 0;
	if(a.toLowerCase()>b.toLowerCase())result =  1;
	if(a.toLowerCase()<b.toLowerCase())result = -1;
	return result;
}