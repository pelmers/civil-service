data "azurerm_key_vault" "cmc_vault" {
  name                = "cmc-${var.env}"
  resource_group_name = "cmc-${var.env}"
}

data "azurerm_key_vault_secret" "db_password_v11_secret" {
  key_vault_id = data.azurerm_key_vault.cmc_vault.id
  name         = "cmc-db-password-v11"
}

resource "azurerm_key_vault_secret" "civil_db_password__v11_secret" {
  name         = "cmc-db-password-v11"
  value        = data.azurerm_key_vault_secret.db_password_v11_secret.value
  key_vault_id = module.key-vault.key_vault_id

  content_type = "secret"
  tags = merge(var.common_tags, {
    "source" : "Vault ${data.azurerm_key_vault.cmc_vault.name}"
  })

  depends_on = [
    module.key-vault
  ]
}