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

const val reveal_input = (
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
        + "};")



const val cleanup = (
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


const val set_reference_url = (
        "var frameset = document.getElementsByTagName('frameset')[2];" +
        "if (frameset) {" +
                "var framecontent = frameset.getElementsByTagName('frame')[0].contentDocument;" +
                "if (framecontent.getElementsByTagName('img')[0]) {" +
                        "var imgsrc = framecontent.getElementsByTagName('img')[0].src;" +
                        "console.log('found image resource '+imgsrc);" +
                        "app.setReferenceUrl(imgsrc);" +
                "} else {" +
                        "console.log('Cannot get reference url');" +
                        "app.onLoadingFail();" +
                "};" +
        "};"
        )

const val get_html = ("(function() { return document.documentElement.outerHTML;})();")

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
        "      };")