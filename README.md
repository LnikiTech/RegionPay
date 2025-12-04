RegionPay is a Minecraft plugin that charges players when they enter a specifi.
Perfect for server

🔧 Features

Automatically charges players when entering configured regions

Per-region pricing using a WorldGuard custom flag

Distributes money to

Fully configurable via config.yml

Lightweight and production-rea

📌 Requirements

Paper / Spigot 1.21.x

Vault (Economy

Any Vault-compatible economy plugin

WorldGuard / WorldEdit

📥 Installation

Download regionpay-1.0.jar

Place it into plugins/

Restart the server

Configure config.yml

⚙️ Configuration

Configuration files are stored in:

/plugins/RegionPay/

Example settings include:

Global tax rate

Region fee messaging

Entry fee values per region

🗺 Required WorldGuard Setup (Important!)
▶ Enable entry charging for a region

RegionPay uses a custom WorldGuard flag:

/rg flag <region> regionpay-entry-fee allow


Example:

/rg flag market regionpay-entry-fee allow

👑 Region Ownership (Who receives the money?)

Region owners will receive the entry fee (minus tax).
Set a region owner with:

/rg addowner <region> <player>

To confirm:

/rg info <rejion>

Disable RegionPay for a Region (temporarily OFF)

Use this if you want to keep the setting but stop charging players:

/rg flag <region> regionpay-entry-fee deny


Example:

/rg flag market regionpay-entry-fee deny


Players will no longer be charged, but you can re-enable later.

Completely Remove RegionPay Flag (reset region)

Use this if you want to remove the entry fee configuration entirely:

/rg flag <region> regionpay-entry-fee -


Example:

/rg flag market regionpay-entry-fee -


This region will no longer be part of RegionPay until configured again.

Re-enable RegionPay (turn charging back ON)
/rg flag <region> regionpay-entry-fee allow

Summary Table
Action	Command
Disable charging	/rg flag <region> regionpay-entry-fee deny
Delete entry-fee flag	/rg flag <region> regionpay-entry-fee -
Enable charging	/rg flag <region> regionpay-entry-fee allow

📘 Commands
Command	Description
/regionpay reload	Reloads configuration files
🧪 Example Setup
/rg define market
/rg addowner market Lniki
/rg flag market regionpay-entry-fee allow

📄 License

This project is licensed under the MIT License.
Use, modify, and distribute freely with attribution.

🤝 Contributing

Suggestions and pull requests are welcome!
Submit them via GitHub Issues or Pull Requests.

📝 Notes / Upcoming Features

Region-based discounts

Additional fee triggers

API expansion
