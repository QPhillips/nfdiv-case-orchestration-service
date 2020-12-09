provider "azurerm" {
  features {}
}

locals {
  vaultName = "${var.product}-${var.env}"
}

module "nfdiv-scheduler-db" {
  source             = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product            = "${var.product}-${var.component}"
  location           = var.location_db
  env                = var.env
  database_name      = "nfdiv_scheduler"
  postgresql_user    = "nfdiv_scheduler"
  postgresql_version = "10"
  sku_name           = "GP_Gen5_2"
  sku_tier           = "GeneralPurpose"
  common_tags        = var.common_tags
  subscription       = var.subscription
}

data "azurerm_key_vault" "key_vault" {
  name = local.vaultName
  resource_group_name = local.vaultName
}

resource "azurerm_key_vault_secret" "postgresql-user" {
  name      = "${var.component}-postgresql-user"
  value     = module.nfdiv-scheduler-db.user_name
  key_vault_id = data.azurerm_key_vault.key_vault.id
}

resource "azurerm_key_vault_secret" "postgresql-password" {
  name      = "${var.component}-postgresql-password"
  value     = module.nfdiv-scheduler-db.postgresql_password
  key_vault_id = data.azurerm_key_vault.key_vault.id
}
