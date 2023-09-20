package com.seventeen.edtinp


var preload = (
        "var framesets = document.getElementsByTagName(\"frameset\");" +// Les frameset sur la raçine
                "var treeFrameset = framesets[1];" +
                "if (treeFrameset) {" +
                "   var treeFrame = treeFrameset.getElementsByTagName(\"frame\")[0];")

var search1A = "   var tosearch = \"1A-PINP\";"
var search2A = "   var tosearch = \"2A-PINP\";"
var searchHN1 = "   var tosearch = \"HN1-PINP\";"
var searchHN2 = "   var tosearch = \"HN2-PINP\";"
var searchHN3 = "   var tosearch = \"HN3-PINP\";"


var load = (
                "   if (treeFrame) {" +
                "      var frameContent = treeFrame.contentWindow.document;" +// Récupère le contenu de la frame de recherche
                "      if (frameContent) {" +
                "         const inputField = frameContent.querySelector('input[name=\"search\"]');" +// Sélectionne l'input dans le contenu du frame
                "         if (inputField) {" +// Vérifie si le champ de saisie et le bouton sont trouvés
                "            inputField.focus();" +
                "            inputField.value = tosearch;" + // Insère la saisie
                "            const inputEvent = new InputEvent(\"input\");" +
                "            inputField.dispatchEvent(inputEvent);" + //simule un click humain
                "            const form = inputField.closest(\"form\");" +
                "            form.submit();" + // Lance la recherche
                "            console.log('Pre-load done (1A)');" +
                "         }" +
                "      }" +
                "   }" +
                "}" +
                "else {" +
                "   console.log('Pre-load aborted')" +
                "};")

const val setup_saturday = (

        "setTimeout(function() {" +
                "console.log('removing saturday');" +
        "var framesets = document.getElementsByTagName(\"frameset\");" +
        "var etFrameset = framesets[2];" +
        "    if (etFrameset) {" +
        "        var etFrames = etFrameset.getElementsByTagName('frame');" +
        "        for (i = 0; i<etFrames.length; i++) {" + // Récupère la frame pianoDays pour supprimer le samedi et le dimanche
        "            if (etFrames[i].name == 'pianoDays') {" +
        "                var etFrame = etFrames[i];" +
        "            }" +
        "        }" +
//        "console.log('frame found: '+etFrame);" +
        "        if (etFrame) {" +
        "            var etFrameContent = etFrame.contentDocument;" +
        "            if (etFrameContent) {" +
        "                var tr = etFrameContent.getElementsByTagName('tr')[0];" +
//        "console.log('tr found: '+tr);" +
        "                if (tr) {" +
        "                    var days = tr.getElementsByTagName('td');" +
//        "console.log('td found '+days);" +

        // Enlève samedi
        "                    if (days[5].className == 'pianoselected') {" +
        "                        var map = days[5].getElementsByTagName('map')[0];" +
        "                        if (map) {" +
        "                            var area = map.getElementsByTagName('area')[0];" +
        "                            if (area) {" +
        "                                area.click();" +
        "                                console.log('removed saturday');" +
        "                            };" +
        "                        };" +
        "                    };" +
        "                    if (days[5].className == 'piano') {" +
        "                        console.log('Not removing saturday');" +
        "                    }" +
        "                }" +
        "            }" +
        "        }" +
        "        else {" +
                "console.log(\"Can't find frame for saturday!\");"+
                "};"+
        "    }" +
        "    else {" +
        "       console.log('Removal aborted (saturday)')" +
        "    };" +
        "}, 0);"

        )

const val setup_sunday = (

//        "setTimeout(function() {" +

        "var framesets = document.getElementsByTagName(\"frameset\");" +
        "var etFrameset = framesets[2];" +
        "    if (etFrameset) {" +
        "        var etFrames = etFrameset.getElementsByTagName('frame');" +
        "        for (i = 0; i<etFrames.length; i++) {" + // Récupère la frame pianoDays pour supprimer le samedi et le dimanche
        "            if (etFrames[i].name == 'pianoDays') {" +
        "                var etFrame = etFrames[i];" +
        "            }" +
        "        }" +
//        "console.log('frame found: '+etFrame);" +
        "        if (etFrame) {" +
        "            var etFrameContent = etFrame.contentDocument;" +
        "            if (etFrameContent) {" +
        "                var tr = etFrameContent.getElementsByTagName('tr')[0];" +
//        "console.log('tr found: '+tr);" +
        "                if (tr) {" +
        "                    var days = tr.getElementsByTagName('td');" +
//        "console.log('td found '+days);" +

        // Enlève dimanche
        "                    if (days[6].className == 'pianoselected') {" +
        "                        var map = days[6].getElementsByTagName('map')[0];" +
        "                        if (map) {" +
        "                            var area = map.getElementsByTagName('area')[0];" +
        "                            if (area) {" +
        "                                area.click();" +
        "                                console.log('removed sunday');" +
        "                            };" +
        "                        };" +
        "                    };" +
        "                    if (days[6].className == 'piano') {" +
        "                        console.log('Not removing sunday');" +
        "                    }" +
        "                }" +
        "            }" +
        "        }" +
        "    }" +
        "    else {" +
        "       console.log('Removal aborted (sunday)')" +
        "    };"

//        + "}, 0);"
        )


val reveal_input = (
        "var outerFrameset = document.getElementsByTagName('frameset')[0];"
        + "if (outerFrameset) {"
        + "    var innerFramesets = outerFrameset.getElementsByTagName('frameset');"
        + "    if (innerFramesets[0]) {"
        + "        var frames = innerFramesets[0].getElementsByTagName('frame');"
        + "        for (var i = 0; i < frames.length; i++) {"
        + "            if (true) {"//frames[i].name === 'tree') {"
        + "                frames[i].style.visibility = 'visible'"
        + "            }"
        + "        }"
        + "    }"
        + "    console.log('Reveal done');"
        + "}"
        + "else {"
        + "   console.log('Reveal aborted')"
        + "};"
        )



val cleanup = (
//        "setTimeout(function() {" +
                "var outerFrameset = document.getElementsByTagName('frameset')[0];"
                + "if (outerFrameset) {"
                + "    var innerFramesets = outerFrameset.getElementsByTagName('frameset');"
                + "    for (var j = 0; j < innerFramesets.length; j++) {"
                + "        if (innerFramesets[j]) {"
                + "            var frames = innerFramesets[j].getElementsByTagName('frame');"
                + "            for (var i = 0; i < frames.length; i++) {"
                + "                if ((frames[i].name == 'pianoWeeks') || (frames[i].name == 'et')) {"
                + "                    frames[i].style.visibility = 'visible';"
                + "                } else {"
                + "                    frames[i].style.visibility = 'hidden';"
                + "                }"
                + "            }"
                + "        }"
                + "    };"
                + "    console.log('Cleanup done');"
                + "}"
                + "else {"
                + "   console.log('Cleanup aborted')"
                + "};"
//                + "}, 3000);"
        )

const val get_selected_week = ("(function() {" +
        "  var frameset = document.getElementsByTagName('frameset')[2];" +
        "  if (frameset) {" +
        "    var frame = frameset.getElementsByTagName('frame')[2];" +
        "    if (frame) {" +
        "      var divs = frame.contentDocument.getElementsByTagName('div');" +
        "      for (i = 0;i<divs.length;i++) {" +
        "        if (divs[i].className == 'pianoselected') {" +
        "          return divs[i].getElementsByTagName('img')[0].alt;" +
        "        }" +
        "      }" +
        "    }" +
        "  };" +
        "  return null;" +
        "})();")

const val get_html = (
        "(function() {" +
//                "return document.documentElement.innerHTML;" +
//                "return document.getElementsByTagName('img').length;" +
                "return document.documentElement.outerHTML;" +
                "})();"
        )

const val check_edt_availability = ("(function() {" +
        "var frameset = document.getElementsByTagName('frameset')[2];" +
        "if (frameset) {" +
        "  var frame = frameset.getElementsByTagName('frame')[0];" +
        "  if (frame) {" +
        "    var result = frame.contentDocument.getElementsByTagName('img');" +
        "    return result.length;" +
        "  };" +
        "};" +
        "})();")

const val set_image_resource = (
//        "function getImgRes()" +
//        "{" +
                "var frameset = document.getElementsByTagName('frameset')[2];" +
                "if (frameset) {" +
                        "var framecontent = frameset.getElementsByTagName('frame')[0].contentDocument;" +
                        "if (framecontent.getElementsByTagName('img')[0]) {" +
                                "var imgsrc = framecontent.getElementsByTagName('img')[0].src;" +
                                "console.log('found image resource '+imgsrc);" +
//                                "app.makeToast('Resource found !', true);" +
                                "app.setImg(imgsrc);" +
                        "} else {" +
                                "console.log('Cannot get image resource');" +
                                "app.onLoadingFail();" +
                        "};" +
                "};"
//        + "};"
        )

const val download_image = (
        "async function downloadImage(imageSrc) {\n" +
//                "  const image = await fetch(imageSrc, { mode: 'no-cors' })\n" +
                "  const image = await fetch(imageSrc)\n" +
                "   .then( response => response.json() )\n" +
                "   .then( data => console.log(data) );" +
                "  const imageBlog = await image.blob()\n" +
                "  console.log(imageBlog.size);" +
                "  return imageBlog" +
                /*"  const imageURL = URL.createObjectURL(imageBlog)\n" +
                "\n" +
                "  const link = document.createElement('a')\n" +
                "  link.href = imageURL\n" +
                "  link.download = 'image file name here'\n" +
                "  document.body.appendChild(link)\n" +
                "  link.click()\n" +
                "  document.body.removeChild(link)\n" +
                "  console.log('got image');" +*/
                "};"
        )


val set_reference_url = (
        "var frameset = document.getElementsByTagName('frameset')[2];" +
        "if (frameset) {" +
                "var framecontent = frameset.getElementsByTagName('frame')[0].contentDocument;" +
                "if (framecontent.getElementsByTagName('img')[0]) {" +
                        "var imgsrc = framecontent.getElementsByTagName('img')[0].src;" +
                        "console.log('found image resource '+imgsrc);" +
//                        "app.makeToast('Reference url found !', true);" +
                        "app.setReferenceUrl(imgsrc);" +
                "} else {" +
                        "console.log('Cannot get reference url');" +
                "};" +
        "};"
        )


const val js_functions =
        ("   function scrollwindow()" +
        "      {" +
        "         window.scroll(450,0);" +
        "         launchImg2();" +
        "      }" +
        "   function changeClassCell(cell, reset)" +
        "      {   " +
        "         if (reset == 'true')" +
        "         {" +
        "            var row = cell.parentNode ;" +
        "            var aCell = row.firstChild ;" +
        "            " +
        "            while (aCell != row.lastChild)" +
        "            {" +
        "               aCell.className = \"piano\" ;" +
        "               aCell = aCell.nextSibling;" +
        "            }" +
        "            aCell.className = \"piano\" ;" +
        "         }" +
                  // change the selected cell
        "         className = cell.className ;" +
        "         if (className == \"piano\")" +
        "         {" +
        "            cell.className = \"pianoselected\" ;" +
        "         }" +
        "         else" +
        "         {" +
        "            cell.className = \"piano\" ;" +
        "         }" +
        "      }" +
        "   function getCurrentPosX()" +
        "      {" +
        "         var X;" +
        "          if(typeof window.pageXOffset != 'undefined')" +
        "          {" +
        "              X = window.pageXOffset;" +
        "          }" +
        "          else" +
        "          {" +
        "                if((!window.document.compatMode)||" +
        "                (window.document.compatMode == 'BackCompat'))" +
        "              {" +
        "                  X = window.document.body.scrollLeft;" +
        "              }" +
        "              else" +
        "              {" +
        "                  X = window.document.documentElement.scrollLeft;" +
        "              }" +
        "          }" +
        "         return X;" +
        "      }" +
        "   function launchImg(id, reset)" +
        "      {" +
        "         top.frames[\"et\"].location='/2023-2024/exterieur/jsp/custom/modules/plannings/bounds.jsp?clearTree=false&week='+id+'&reset='+reset;" +
        "      }" +
        "      " +
        "   function launchImg2()" +
        "      {" +
        "         top.frames[\"et\"].location='/2023-2024/exterieur/jsp/custom/modules/plannings/bounds.jsp?clearTree=false';" +
        "      }" +
        "   function push(id, reset)" +
        "      {" +
        "         launchImg(id, reset);" +
                /*"        setTimeout(function() {" +
                "        var frameset = document.getElementsByTagName('frameset')[2];" +
                "        var framecontent = frameset.getElementsByTagName('frame')[0].contentDocument;" +
                "        var imgsrc = framecontent.getElementsByTagName('img')[0].src;" +
                "        app.setImg(imgsrc)}, 100);" +*/
//                "        return null;" +
                  // Highlight some buttons
//        "        var frameset = document.getElementsByTagName('frameset')[2];" +
//        "        var framecontent = frameset.getElementsByTagName('frame')[2].contentDocument;" +
//        "         var imgsrc = new String (framecontent.images[id].src) ;" +
//        "         var index = imgsrc.indexOf(\"&\",0);" +
//        "         var newImgsrc = imgsrc.substring(0, index);" +
//        "         if (reset == 'true')" +
//        "         {" +
//        "            for (i=0; i<framecontent.images.length; i++)" +
//        "            {" +
//        "               var anImgsrc = new String(framecontent.images[i].src) ;" +
//        "               var anIndex = anImgsrc.indexOf(\"&\",0);" +
//        "               var aNewImgsrc = anImgsrc.substring(0, anIndex);" +
//        "               var aNewImgsrcFin = anImgsrc.substring(anIndex+1);" +
//        "               if ((aNewImgsrcFin == \"cssClass=div.pianoselected\")||(aNewImgsrcFin == \"cssClass=div.pianoselected&cssClassPlus=td.pianoPlus\"))" +
//        "               {" +
//        "                  aNewImgsrc = aNewImgsrc + '&cssClass=div.piano' ;" +
//        "                  framecontent.images[i].src = aNewImgsrc ;" +
//        "               }" +
//        "            }" +
//        "            newImgsrc = newImgsrc + '&cssClass=div.pianoselected' ;" +
//        "            framecontent.images[id].src = newImgsrc ;" +
//        "         }" +
//        "         else" +
//        "         {" +
//        "            var newImgsrcFin = imgsrc.substring(index+1);" +
//        "            if (newImgsrcFin == \"cssClass=div.pianoselected\")" +
//        "            {" +
//        "               newImgsrc = newImgsrc + '&cssClass=div.piano' ;" +
//        "               framecontent.images[id].src = newImgsrc ;" +
//        "            }" +
//        "            else" +
//        "            {" +
//        "               newImgsrc = newImgsrc + '&cssClass=div.pianoselected' ;" +
//        "               framecontent.images[id].src = newImgsrc ;" +
//        "            }" +
//        "         };" +
        "      };")