RegionPay is a Minecraft plugin that charges players when they enter a specified WorldGuard region.
Useful for servers that want to create toll areas, paid zones, restricted access areas, or economic-based gameplay regions.

🔧 Features

Automatically charges players when entering configured regions

Fully customizable per-region pricing

Easy configuration using config.yml and region.yml

Lightweight and optimized for production servers

📌 Requirements

Paper / Spigot 1.21.x

Vault (Economy API)

Economy plugin compatible with Vault

WorldGuard / WorldEdit

📥 Installation

Download regionpay-1.0.jar

Place it into your server plugins folder

Restart the server

Edit configuration files as needed

⚙️ Configuration

Located in:

/plugins/RegionPay/

Example options:

Entry fees per region

Cooldowns to prevent repeated charging

Global settings (messages, defaults, etc.)

📘 Usage / Commands
Command	Description
/regionpay reload	Reloads configuration files
🗺 WorldGuard Setup (Important!)

To enable charging in a region, apply this custom WorldGuard flag:

▶ Enable entry fee
/rg flag <region> regionpay-entry-fee allow


Example:

/rg flag market regionpay-entry-fee allow

▶ Allow passthrough (optional)

Useful when players should walk through shops/hubs without building.

/rg flag -w <world> <region> passthrough allow


Example:

/rg flag -w world market passthrough allow

▶ Disable mob spawning (optional)
/region flag <region> mob-spawning deny

🧪 Example Setup
/rg define market
/rg addowner market Lniki
/rg flag market regionpay-entry-fee allow
/rg flag -w world market passthrough allow
/region flag market mob-spawning deny

📄 License

This project is licensed under the MIT License.
You may use, modify, and distribute the project as long as attribution is preserved.

🤝 Contributing

Suggestions and pull requests are welcome!
Please submit them through the GitHub repository.

📝 Notes

Planned future features:

Region-based discounts

Additional fee event triggers

API expansions
