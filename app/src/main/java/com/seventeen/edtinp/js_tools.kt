package com.seventeen.edtinp


var preload_1A = ("console.log('Pre-load beginning');" +
        "var framesets = document.getElementsByTagName(\"frameset\");" +// Les frameset sur la raçine
        "var treeFrameset = framesets[1];" +
        "var treeFrame = treeFrameset.getElementsByTagName(\"frame\")[0];" +
        "var tosearch = \"1A-PINP\";" +
        "var frameContent = treeFrame.contentWindow.document;" +// Récupère le contenu de la frame de recherche
        "const inputField = frameContent.querySelector('input[name=\"search\"]');" +// Sélectionne l'input dans le contenu du frame
        "if (inputField) {" +// Vérifie si le champ de saisie et le bouton sont trouvés
        "    inputField.focus();" +
        "    inputField.value = tosearch;" + // Insère la saisie
        "    const inputEvent = new InputEvent(\"input\");" +
        "    inputField.dispatchEvent(inputEvent);" + //simule un click humain
        "    const form = inputField.closest(\"form\");" +
        "    form.submit();" + // Lance la recherche
        "};" +
        "console.log('Pre-load finished');")

const val preload_2A = ("console.log('Pre-load beginning');" +
        "var framesets = document.getElementsByTagName(\"frameset\");" +// Les frameset sur la raçine
        "var treeFrameset = framesets[1];" +
        "var treeFrame = treeFrameset.getElementsByTagName(\"frame\")[0];" +
        "var tosearch = \"2A-PINP\";" +
        "var frameContent = treeFrame.contentWindow.document;" +// Récupère le contenu de la frame de recherche
        "const inputField = frameContent.querySelector('input[name=\"search\"]');" +// Sélectionne l'input dans le contenu du frame
        "if (inputField) {" +// Vérifie si le champ de saisie et le bouton sont trouvés
        "    inputField.focus();" +
        "    inputField.value = tosearch;" + // Insère la saisie
        "    const inputEvent = new InputEvent(\"input\");" +
        "    inputField.dispatchEvent(inputEvent);" + //simule un click humain
        "    const form = inputField.closest(\"form\");" +
        "    form.submit();" + // Lance la recherche
        "};" +
        "console.log('Pre-load finished');")

const val setup_saturday = (

//        "setTimeout(function() {" +
        "    console.log('Removing saturday');" +
        "    var framesets = document.getElementsByTagName(\"frameset\");" +
        "    var etFrameset = framesets[2];" +
        "    var etFrames = etFrameset.getElementsByTagName('frame');" +
        "    for (i = 0; i<etFrames.length; i++) {" + // Récupère la frame pianoDays pour supprimer le samedi et le dimanche
        "        if (etFrames[i].name == 'pianoDays') {" +
        "            var etFrame = etFrames[i];" +
        "        }" +
        "    };" +
//        "console.log('frame found: '+etFrame);" +
        "    var etFrameContent = etFrame.contentDocument;" +
        "    var tr = etFrameContent.getElementsByTagName('tr')[0];" +
//        "console.log('tr found: '+tr);" +
        "    var days = tr.getElementsByTagName('td');" +
//        "console.log('td found '+days);" +

        // Enlève samedi
        "    if (days[5].className == 'pianoselected') {" +
        "        var map = days[5].getElementsByTagName('map')[0];" +
        "        var area = map.getElementsByTagName('area')[0];" +
        "        if (area) {" +
        "            area.click();" +
        "            console.log('removed saturday');" +
        "        };" +
        "    };" +
        "    if (days[5].className == 'piano') {" +
        "        console.log('Nothing to remove');" +
        "    };"
//        + "}, 0);"
        )

const val setup_sunday = (

//        "setTimeout(function() {" +
                "    console.log('Removing sunday');" +
                "    var framesets = document.getElementsByTagName(\"frameset\");" +
                "    var etFrameset = framesets[2];" +
                "    var etFrames = etFrameset.getElementsByTagName('frame');" +
                "    for (i = 0; i<etFrames.length; i++) {" + // Récupère la frame pianoDays pour supprimer le samedi et le dimanche
                "        if (etFrames[i].name == 'pianoDays') {" +
                "            var etFrame = etFrames[i];" +
                "        }" +
                "    };" +
//        "console.log('frame found: '+etFrame);" +
                "    var etFrameContent = etFrame.contentDocument;" +
                "    var tr = etFrameContent.getElementsByTagName('tr')[0];" +
//        "console.log('tr found: '+tr);" +
                "    var days = tr.getElementsByTagName('td');" +
//        "console.log('td found '+days);" +

                // Enlève samedi
                "    if (days[6].className == 'pianoselected') {" +
                "        var map = days[6].getElementsByTagName('map')[0];" +
                "        var area = map.getElementsByTagName('area')[0];" +
                "        if (area) {" +
                "            area.click();" +
                "            console.log('removed saturday');" +
                "        };" +
                "    };" +
                "    if (days[5].className == 'piano') {" +
                "        console.log('Nothing to remove');" +
                "    };"
//                + "}, 0);"
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
                + "                if (frames[i].name === 'et') {"
                + "                    frames[i].style.visbillity = 'show';"
                + "                } else {"
                + "                    frames[i].style.visibility = 'hidden';"
                + "                }"
                + "            }"
                + "        }"
                + "    }"
                + "};"
                + "console.log('Cleanup done');"
//                + "}, 3000);"
        )

const val getFromPage = "(function() { " +
        "var divs = document.getElementsByTagName('frameset')[2].getElementsByTagName('frame')[2].contentDocument.getElementsByTagName('div');" +
        "for (i = 0;i<divs.length;i++) {" +
        "    if (divs[i].className == 'pianoselected') {" +
        "        return divs[i].getElementsByTagName('img')[0].alt;" +
        "    }" +
        "}" +
        "})();"


const val js_functions = ("\n" +
        "\t\tfunction scrollwindow()\n" +
        "\t\t{\n" +
        "\t\t\t\n" +
        "\t\t\twindow.scroll(450,0);\n" +
        "\t\t\t\n" +
        "\t\t\t// launch img\n" +
        "\t\t\tlaunchImg2();\n" +
        "\t\t}\n" +
        "\n" +
        "\t\tfunction changeClassCell(cell, reset)\n" +
        "\t\t{\t\n" +
        "\t\t\t//Reset\n" +
        "\t\t\t//\n" +
        "\t\t\tif (reset == 'true')\n" +
        "\t\t\t{\n" +
        "\t\t\t\tvar row = cell.parentNode ;\n" +
        "\t\t\t\tvar aCell = row.firstChild ;\n" +
        "\t\t\t\t\n" +
        "\t\t\t\twhile (aCell != row.lastChild)\n" +
        "\t\t\t\t{\n" +
        "\t\t\t\t\taCell.className = \"piano\" ;\n" +
        "\t\t\t\t\taCell = aCell.nextSibling;\n" +
        "\t\t\t\t}\n" +
        "\t\t\t\taCell.className = \"piano\" ;\n" +
        "\t\t\t}\n" +
        "\t\t\t\n" +
        "\t\t\t// change the selected cell\n" +
        "\t\t\t//\n" +
        "\t\t\tclassName = cell.className ;\n" +
        "\t\t\tif (className == \"piano\")\n" +
        "\t\t\t{\n" +
        "\t\t\t\tcell.className = \"pianoselected\" ;\n" +
        "\t\t\t}\n" +
        "\t\t\telse\n" +
        "\t\t\t{\n" +
        "\t\t\t\tcell.className = \"piano\" ;\n" +
        "\t\t\t}\n" +
        "\t\t}\n" +
        "\t\t\n" +
        "\t\tfunction getCurrentPosX()\n" +
        "\t\t{\n" +
        "\t\t\tvar X;\n" +
        "\t\t    if(typeof window.pageXOffset != 'undefined')\n" +
        "\t\t    {\n" +
        "\t\t        X = window.pageXOffset;\n" +
        "\t\t    }\n" +
        "\t\t    else\n" +
        "\t\t    {\n" +
        "\t\t       \tif((!window.document.compatMode)||\n" +
        "\t\t          (window.document.compatMode == 'BackCompat'))\n" +
        "\t\t        {\n" +
        "\t\t            X = window.document.body.scrollLeft;\n" +
        "\t\t        }\n" +
        "\t\t        else\n" +
        "\t\t        {\n" +
        "\t\t            X = window.document.documentElement.scrollLeft;\n" +
        "\t\t        }\n" +
        "\t\t    }\n" +
        "\t\t\treturn X;\n" +
        "\t\t}\n" +
        "\t\t\n" +
        "\t\tfunction launchImg(id, reset)\n" +
        "\t\t{\n" +
        "\t\t\ttop.frames[\"et\"].location='/2023-2024/exterieur/jsp/custom/modules/plannings/bounds.jsp?clearTree=false&week='+id+'&reset='+reset;\n" +
        "\t\t}\n" +
        "\t\t\n" +
        "\t\tfunction launchImg2()\n" +
        "\t\t{\n" +
        "\t\t\ttop.frames[\"et\"].location='/2023-2024/exterieur/jsp/custom/modules/plannings/bounds.jsp?clearTree=false';\n" +
        "\t\t}\n" +
        "\t\t\n" +
        "\t\tfunction push(id, reset)\n" +
        "\t\t{\n" +
        "\t\t\tlaunchImg(id, reset) ;\n" +
        "\t\t\t\n" +
        "\t\t\t// Highlight some buttons\n" +
        "\t\t\t//\n" +
        "\t\t\tvar imgsrc = new String (document.images[id].src) ;\n" +
        "\t\t\tvar index = imgsrc.indexOf(\"&\",0);\n" +
        "\t\t\tvar newImgsrc = imgsrc.substring(0, index);\n" +
        "\t\t\t\n" +
        "\t\t\tif (reset == 'true')\n" +
        "\t\t\t{\n" +
        "\t\t\t\tfor (i=0; i<document.images.length; i++)\n" +
        "\t\t\t\t{\n" +
        "\t\t\t\t\tvar anImgsrc = new String(document.images[i].src) ;\n" +
        "\t\t\t\t\tvar anIndex = anImgsrc.indexOf(\"&\",0);\n" +
        "\t\t\t\t\tvar aNewImgsrc = anImgsrc.substring(0, anIndex);\n" +
        "\t\t\t\t\tvar aNewImgsrcFin = anImgsrc.substring(anIndex+1);\n" +
        "\t\t\t\t\t\n" +
        "\t\t\t\t\tif ((aNewImgsrcFin == \"cssClass=div.pianoselected\")||(aNewImgsrcFin == \"cssClass=div.pianoselected&cssClassPlus=td.pianoPlus\"))\n" +
        "\t\t\t\t\t{\n" +
        "\t\t\t\t\t\taNewImgsrc = aNewImgsrc + '&cssClass=div.piano' ;\n" +
        "\t\t\t\t\t\tdocument.images[i].src = aNewImgsrc ;\n" +
        "\t\t\t\t\t}\n" +
        "\t\t\t\t}\n" +
        "\t\t\t\t\n" +
        "\t\t\t\tnewImgsrc = newImgsrc + '&cssClass=div.pianoselected' ;\n" +
        "\t\t\t\tdocument.images[id].src = newImgsrc ;\n" +
        "\t\t\t}\n" +
        "\t\t\telse\n" +
        "\t\t\t{\n" +
        "\t\t\t\tvar newImgsrcFin = imgsrc.substring(index+1);\n" +
        "\t\t\t\tif (newImgsrcFin == \"cssClass=div.pianoselected\")\n" +
        "\t\t\t\t{\n" +
        "\t\t\t\t\tnewImgsrc = newImgsrc + '&cssClass=div.piano' ;\n" +
        "\t\t\t\t\tdocument.images[id].src = newImgsrc ;\n" +
        "\t\t\t\t}\n" +
        "\t\t\t\telse\n" +
        "\t\t\t\t{\n" +
        "\t\t\t\t\tnewImgsrc = newImgsrc + '&cssClass=div.pianoselected' ;\n" +
        "\t\t\t\t\tdocument.images[id].src = newImgsrc ;\n" +
        "\t\t\t\t}\n" +
        "\t\t\t}\n" +
        "\t\t};\n")