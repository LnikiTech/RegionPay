# RegionPay

RegionPay is a Minecraft plugin that charges players when they enter a specified WorldGuard region.
Perfect for servers that want toll areas, paid zones, restricted access, or player-driven economies.

---

## Features

- Automatically charges players upon entering configured regions
- Per-region pricing using a WorldGuard custom flag
- Region owners receive income (minus tax)
- Fully configurable via config.yml
- Lightweight and production-ready

---

## Requirements

- Paper / Spigot 1.21.x
- Vault (Economy API)
- Any Vault-compatible economy plugin
- WorldGuard / WorldEdit

---

## Installation

1. Download regionpay-1.0.jar
2. Place it into the plugins folder
3. Restart the server
4. Configure config.yml

---

## Configuration

Configuration files are stored in:

/plugins/RegionPay/

Configurable options include:

- Global tax rate
- Region entry messages
- Entry fee values per region

---

## WorldGuard Setup

Enable entry charging for a region:

/rg flag <region> regionpay-entry-fee allow

Example:

/rg flag market regionpay-entry-fee allow

---

## Region Ownership

Region owners receive the entry fee (minus tax).

Set a region owner:

/rg addowner <region> <player>

Check region info:

/rg info <region>

---

## Disable / Remove / Enable RegionPay

Disable charging:

/rg flag <region> regionpay-entry-fee deny

Remove the flag:

/rg flag <region> regionpay-entry-fee -

Enable charging:

/rg flag <region> regionpay-entry-fee allow

---

## Plugin Commands

/regionpay reload  Reload configuration files

---

## Example Setup

/rg define market
/rg addowner market Lniki
/rg flag market regionpay-entry-fee allow

---

## License

This project is licensed under the MIT License.

---

## Contributing

Suggestions and pull requests are welcome via GitHub Issues or Pull Requests.

---

## Notes / Upcoming Features

- Region-based discounts
- Additional fee triggers
- API expansion