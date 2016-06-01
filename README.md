#Jecon
Economy base plugin for Bukkit/Spigot

##Characteristic
* UUID Ready
* 1.9 Ready
* Vault Ready
* MySQL(+SQLite) Ready

##Installation
1. [Download this plugin.](https://github.com/HimaJyun/Jecon/releases/latest "Get Jecon")
2. drop in plugins directory.
3. Server start.
4. config edit.
5. reload

##Settings
Please refer to the comments in the configuration file.

##Command/Permission
|Command                                   |Permission      |Description                              |Default|
|:-----------------------------------------|:---------------|:----------------------------------------|:------|
|/money                                    |jecon.show      |Show your balance.                       |ALL    |
|/money [player]                           |jecon.show.other|Show [player] balance.                   |OP     |
|/money pay &lt;player&gt; &lt;amount&gt;  |jecon.pay       |Send &lt;amount&gt; to &lt;player&gt;.   |ALL    |
|/money top [page]                         |jecon.top       |Show the top list.                       |OP     |
|/money give &lt;player&gt; &lt;amount&gt; |jecon.give      |Give &lt;amount&gt; to &lt;player&gt;.   |OP     |
|/money take &lt;player&gt; &lt;amount&gt; |jecon.take      |Take &lt;amount&gt; on &lt;player&gt;.   |OP     |
|/money set &lt;player&gt; &lt;amount&gt;  |jecon.set       |Set the &lt;amount&gt; of &lt;player&gt;.|OP     |
|/money create &lt;player&gt; [amount]     |jecon.create    |Create &lt;player&gt; account.           |OP     |
|/money remove &lt;player&gt;              |jecon.remove    |Remove &lt;player&gt; account.           |OP     |
|/money reload                             |jecon.reload    |Reload the config.                       |OP     |
|/money help                               |N/A             |Show command help.                       |ALL    |