RegionPay

RegionPay is a Minecraft plugin that charges players when they enter a specified WorldGuard region. Perfect for servers that want toll areas, paid zones, restricted access, or player-driven economies.

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

Download the latest release: RegionPay v1.0

Place the regionpay-1.0.jar into the plugins/ folder

Restart the server

Configure config.yml as needed

⚙️ Configuration

Configuration files are stored in: /plugins/RegionPay/

Example settings:

Global tax rate

Region entry messages

Entry fee values per region

🗺 WorldGuard Setup (Important!)
Enable entry charging for a region

RegionPay uses a custom WorldGuard flag:

/rg flag regionpay-entry-fee allow


Example:

/rg flag market regionpay-entry-fee allow

👑 Region Ownership (Who receives the money?)

Region owners receive the entry fee (minus tax). Set a region owner with:

/rg addowner <region> <player>


To confirm ownership:

/rg info <region>

🛑 Disable / Remove / Enable RegionPay
Action	Command
Disable charging (temporarily OFF)	/rg flag regionpay-entry-fee deny
Delete entry flag (reset the region)	/rg flag regionpay-entry-fee -
Re-enable charging	/rg flag regionpay-entry-fee allow

Example for the market region:

/rg flag market regionpay-entry-fee deny
/rg flag market regionpay-entry-fee -
/rg flag market regionpay-entry-fee allow

📘 Plugin Commands
Command	Description
/regionpay reload	Reloads configuration files
🧪 Example Setup
/rg define market
/rg addowner market Lniki
/rg flag market regionpay-entry-fee allow

📄 License

This project is licensed under the MIT License. You may use, modify, and distribute it as long as attribution is preserved.

🤝 Contributing

Suggestions and pull requests are welcome! Submit them via GitHub Issues or Pull Requests.

📝 Notes / Upcoming Features

Region-based discounts

Additional fee triggers

API expansion
