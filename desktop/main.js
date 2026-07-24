const {app, BrowserWindow, ipcMain, shell, session} = require('electron');
const path = require('path');

let win;
const HOME = 'file://' + path.join(__dirname, 'renderer', 'home.html');

function createWindow(){
  win = new BrowserWindow({
    width: 1280, height: 820, minWidth: 720, minHeight: 520,
    title: 'JULE Browser',
    webPreferences:{preload:path.join(__dirname,'preload.js'),contextIsolation:true,nodeIntegration:false,sandbox:true}
  });
  win.loadURL(HOME);

  // Keep every HTTP/HTTPS activity in the JULE frame.
  win.webContents.setWindowOpenHandler(({url}) => {
    if (/^https?:\/\//i.test(url)) { win.loadURL(url); return {action:'deny'}; }
    if (/^(mailto:|tel:)/i.test(url)) shell.openExternal(url);
    return {action:'deny'};
  });
  win.webContents.on('will-navigate',(event,url)=>{
    if (url === HOME || /^https?:\/\//i.test(url)) return;
    event.preventDefault();
  });
}

app.whenReady().then(()=>{
  session.defaultSession.setPermissionRequestHandler((_wc,permission,callback)=>{
    callback(['media','geolocation','notifications'].includes(permission));
  });
  createWindow();
  app.on('activate',()=>{if(BrowserWindow.getAllWindows().length===0)createWindow();});
});
app.on('window-all-closed',()=>{if(process.platform!=='darwin')app.quit();});

ipcMain.handle('nav:open',(_e,url)=>win.loadURL(url));
ipcMain.handle('nav:back',()=>{if(win.webContents.canGoBack())win.webContents.goBack();});
ipcMain.handle('nav:forward',()=>{if(win.webContents.canGoForward())win.webContents.goForward();});
ipcMain.handle('nav:reload',()=>win.webContents.reload());
ipcMain.handle('nav:home',()=>win.loadURL(HOME));
