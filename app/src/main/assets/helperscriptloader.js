var jquery = document.createElement('script');
jquery.type = 'text/javascript';
jquery.src = 'file:///android_asset/jquery.js';
jquery.onload = function () {
    $(document).ready(function () {
     $(document).on("click", "img",
     function(event){
     var imageSource;
     if (event.target.getAttribute("full_src") == null)
     {
        imageSource = event.target.getAttribute("src");
     }
     else
     {
        imageSource = event.target.getAttribute("full_src");
     }
        lightNoteHelper.openImageAtActivity(imageSource);
     });
     function clickBody() {
         $("#editarea").focus();
     }
     document.body.addEventListener("click", clickBody)
});

};
document.body.appendChild(jquery);



function removeAllScripts()
{

        var scripts = document.getElementsByTagName('script');

        for (i = 0; i < scripts.length; i++) {
          scripts[i].parentNode.removeChild(scripts[i]);
        }
}
function removeUnnecessaryImageFiles()
{
    elements = document.getElementsByTagName("img");
    m = ['',''];
    for (i = 0;i < elements.length;i++) {
        m.push(elements[i].getAttribute("src"));
        if (elements[i].getAttribute("full_src") != null)
        {
            m.push(elements[i].getAttribute("full_src"));
        }
    }
    lightNoteHelper.RemoveFiles(m);
}
