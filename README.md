RegionPay

RegionPay is a Minecraft plugin that charges players when they enter a specified WorldGuard region.
Perfect for servers that want toll areas, paid zones, restricted access, or player-driven economies.

🔧 Features

Automatically charges players upon entering configured regions

Per-region pricing using a WorldGuard custom flag

Region owners receive income (minus tax)

Fully configurable via config.yml

Lightweight and production-ready

📌 Requirements

Paper / Spigot 1.21.x

Vault (Economy API)

Any Vault-compatible economy plugin

WorldGuard / WorldEdit

📥 Installation

Download regionpay-1.0.jar

Place it into the plugins/ folder

Restart the server

Configure config.yml

⚙️ Configuration

Configuration files are stored in:
/plugins/RegionPay/

Example settings:

Global tax rate

Region entry messages

Entry fee values per region

🗺 WorldGuard Setup (Important!)
▶ Enable entry charging for a region

RegionPay uses a WorldGuard custom flag:

/rg flag <region> regionpay-entry-fee allow


Example:

/rg flag market regionpay-entry-fee allow

👑 Region Ownership (Who receives the money?)

Region owners receive the entry fee (minus tax).
Set a region owner:

/rg addowner <region> <player>


To confirm ownership:

/rg info <region>

🛑 Disable / Remove / Enable RegionPay
Disable RegionPay for a region (temporarily OFF)
/rg flag <region> regionpay-entry-fee deny


Example:

/rg flag market regionpay-entry-fee deny

Completely remove RegionPay flag (reset the region)
/rg flag <region> regionpay-entry-fee -


Example:

/rg flag market regionpay-entry-fee -

Re-enable RegionPay
/rg flag <region> regionpay-entry-fee allow

Summary Table
Action	Command
Disable charging	/rg flag <region> regionpay-entry-fee deny
Delete entry flag	/rg flag <region> regionpay-entry-fee -
Enable charging	/rg flag <region> regionpay-entry-fee allow
📘 Plugin Commands
Command	Description
/regionpay reload	Reloads configuration files
🧪 Example Setup
/rg define market
/rg addowner market Lniki
/rg flag market regionpay-entry-fee allow

📄 License

This project is licensed under the MIT License.
You may use, modify, and distribute it as long as attribution is preserved.

🤝 Contributing

Suggestions and pull requests are welcome!
Submit them via GitHub Issues or Pull Requests.

📝 Notes / Upcoming Features

Region-based discounts

Additional fee triggers

API expansion
