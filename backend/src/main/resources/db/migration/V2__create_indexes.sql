CREATE INDEX idx_configurations_owner ON configurations(owner_id);
CREATE INDEX idx_configurations_visibility_updated ON configurations(visibility, updated_at DESC);
CREATE INDEX idx_components_category_name ON components(category, name);
CREATE INDEX idx_components_manufacturer ON components(manufacturer);
CREATE INDEX idx_offers_component_price ON offers(component_id, price_eur);
CREATE INDEX idx_offers_stock_status ON offers(stock_status);
CREATE INDEX idx_refresh_tokens_family ON refresh_tokens(family_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);

