<script src="<? $assets->getAsset('sidebarMenu/sidebar.menu.v2.js') ?>"></script>
<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init([<?php
                printf('"%s", "%s", %d, %s, %s', $menuObj->aiName, $menuObj->topLevelHighlight,
                    $menuObj->subMenuHighlight, $menuObj->blockedClicked ? "true" : "false",
                    $menuObj->limited ? "true" : "false");?>]);
    </script>
</form>