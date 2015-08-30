var MY_PKG="com.faendir.lightning_launcher.appdrawer";
var TAG = "appdrawer_lukas";

// install (or update) a script given its id in the package, and its clear name in the launcher data
function installScript(id,name){
	// load the script (if any) among the existing ones
	var script=LL.getScriptByName(name);

	var script_text=LL.loadRawResource(MY_PKG,id);

	if(script==null){
		// script not found: install it
		script=LL.createScript(name,script_text,0);
	}else{
		// the script already exists: update its text
		script.setText(script_text);
	}

	return script;
}


var script=installScript("appdrawer","AppDrawer_main");
var menu=installScript("menu","AppDrawer_menu");
if(!confirm("Do you want to use this container as App Drawer?"))return;
var c=LL.getEvent().getContainer();
c.getProperties().edit().setEventHandler("resumed",EventHandler.RUN_SCRIPT,script.getId()).setEventHandler("bgLongTap",EventHandler.RUN_SCRIPT,menu.getId()).commit();
Android.makeNewToast("Enjoy your new Appdrawer!",true).show();
LL.runScript(script.getName(),"reload");