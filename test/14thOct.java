/**
 * 
 
*
* 




*



* 
 */
package uk.co.tui.shortlist.populators;

import de.hybris.platform.commerceservices.converter.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.util.Config;
import a.b.c.d;
import java.math.BigDecimal;
import java.util.Currency;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import uk.co.portaltech.commons.StringUtil;
import uk.co.portaltech.commons.SyntacticSugar;
import uk.co.tui.book.domain.lite.BaggageExtraFacility;
import uk.co.tui.book.domain.lite.BaggageExtraFacilityRestrictions;
import uk.co.tui.book.domain.lite.ExtraFacility;
import uk.co.tui.book.domain.lite.ExtraFacilityRestrictions;
import uk.co.tui.book.domain.lite.FacilitySelectionType;
import uk.co.tui.book.domain.lite.Money;
import uk.co.tui.book.domain.lite.Price;
import uk.co.tui.shortlist.constants.SavedHolidayExtraConstants;
import uk.co.tui.shortlist.constants.ShortlistConstants;
import uk.co.tui.shortlist.view.data.SavedHolidayBaggageExtraViewData;
import uk.co.tui.shortlist.view.data.SavedHolidayExtraViewData;


/**
 * @author Sravani
 * 
 */
public class SavedHolidayExtrasViewDataPopulator implements Populator<ExtraFacility, SavedHolidayExtraViewData>
{

	/** The summary panel property reader. */
	/*
	 * @Resource private CaPropertyReader caPropertyReader;
	 */

	/** The static page content service. */
	/*
	 * @Resource private StaticContentService staticContentServ;
	 */

	@SuppressWarnings(ShortlistConstants.BOXING)
	@Override
	public void populate(final ExtraFacility source, final SavedHolidayExtraViewData target) throws ConversionException
	{
		target.setCode(source.getExtraFacilityCode());
		target.setGroupCode(source.getExtraFacilityGroup().toString());
		target.setIncluded(isExtraIncluded(source));
		target.setFree(isExtraFree(source));
		target.setSelected(source.isSelected());
		target.setDescription(populateDescrption(source));
		target.setSelection(StringUtils.lowerCase(source.getSelection().toString()));
		target.setCategoryCode(source.getExtraFacilityCategory().getCode());
		target.setAvailableQuantity(source.getQuantity() != null ? source.getQuantity().intValue() : 0);
		if (StringUtils.equalsIgnoreCase(source.getExtraFacilityCategory().getCode(),
				SavedHolidayExtraConstants.BAGGAGE_EXTRA_CATEGORY_CODE))
		{
			populateBaggageExtraFacilityViewData(source, target);
		}
		else
		{
			populateOtherExtraFacilityViewData(source, target);
		}
		//populateSummaryDescription(source,target);
	}

	/**
	 * @param source
	 * @param target
	 */
	@SuppressWarnings(ShortlistConstants.BOXING)
	private void populateBaggageExtraFacilityViewData(final ExtraFacility source, final SavedHolidayExtraViewData target)
	{
		final SavedHolidayBaggageExtraViewData bagViewData = (SavedHolidayBaggageExtraViewData) target;
		final BaggageExtraFacility bagModel = (BaggageExtraFacility) source;
		final BaggageExtraFacilityRestrictions restrictionModel = bagModel.getBaggageExtraFacilityRestrictions();
		bagViewData.setPrice(bagModel.getPrice().getRate().getAmount());
		if (SyntacticSugar.isNotNull(restrictionModel))
		{
			bagViewData.setMaxPiecePerPerson(restrictionModel.getMaxPieceperPax());
			bagViewData.setMaxWeightPerPiece(restrictionModel.getMaxWeightPerPiece());
			bagViewData.setInfantWeight(restrictionModel.getInfantWeight());
			bagViewData.setBaseWeight(restrictionModel.getBaseWeight());
			bagViewData.setWeightCode(Integer.valueOf(bagModel.getBaggageWeight()).toString());
			bagViewData.setInfantBaggageWeightDescription(getDescription("INFANT_BAG_DESCRIPTION"));
			bagViewData.setInfantBaggageWeightSelection("free");
		}
	}

	/**
	 * @param source
	 * @param target
	 */
	@SuppressWarnings(ShortlistConstants.BOXING)
	private void populateOtherExtraFacilityViewData(final ExtraFacility source, final SavedHolidayExtraViewData target)
	{
		final ExtraFacilityRestrictions restrictionModel = source.getExtraFacilityRestrictions();
		final Price priceModel = source.getPrices().get(0);
		target.setAdultPrice(priceModel.getRate().getAmount());
		final Integer quantity = priceModel.getQuantity();
		populatePrice(priceModel, target);
		populateAgeRestriction(restrictionModel, target);
		populatePaxType(source, target);
		if (StringUtil.isNotEquals(source.getExtraFacilityCategory().getCode(), SavedHolidayExtraConstants.DONATION_CATEGORY))
		{
			final int quant = (quantity == null) ? 0 : quantity.intValue();
			target.setQuantity(quant);
			target.setSelectedQuantity(quant);
		}
		populateQuantity(source, target);
	}

	/**
	 * @param source
	 * @param target
	 */
	private void populatePaxType(final ExtraFacility source, final SavedHolidayExtraViewData target)
	{
		if (StringUtils.equalsIgnoreCase(source.getExtraFacilityCategory().getSuperCategoryCode(),
				SavedHolidayExtraConstants.EXCURSION)
				|| StringUtils.equalsIgnoreCase(source.getExtraFacilityCategory().getSuperCategoryCode(),
						SavedHolidayExtraConstants.ATTRACTION))
		{
			final String paxType = source.getPrices().get(0).getPriceType().toString();
			target.setPaxType(WordUtils.capitalize(StringUtils.lowerCase(paxType)));

		}
	}

	/**
	 * @param source
	 * @param target
	 */
	@SuppressWarnings("boxing")
	private void populateQuantity(final ExtraFacility source, final SavedHolidayExtraViewData target)
	{
		final Price adultPriceModel = source.getPrices().get(0);

		if (source.getPrices().size() > 1)
		{
			final Price childPriceModel = source.getPrices().get(1);
			if (childPriceModel.getQuantity() != null)
			{
				target.setSelectedQuantity(target.getQuantity() + childPriceModel.getQuantity().intValue());
				target.setQuantity(target.getQuantity() + childPriceModel.getQuantity().intValue());
			}
			target.setChildPrice(source.getPrices().get(1).getRate().getAmount());
		}
		else if (adultPriceModel.getQuantity() != null)
		{
			target.setSelectedQuantity(adultPriceModel.getQuantity().intValue());
		}
	}

	/**
	 * @param restrictionModel
	 * @param target
	 */
	@SuppressWarnings("boxing")
	private void populateAgeRestriction(final ExtraFacilityRestrictions restrictionModel, final SavedHolidayExtraViewData target)
	{
		target.setMaxChildAge(restrictionModel.getMaxAge() != null ? restrictionModel.getMaxAge().intValue() : 0);
		target.setMinAge(restrictionModel.getMinAge() != null ? restrictionModel.getMinAge().intValue() : 0);
	}

	/**
	 * @param priceModel
	 * @param target
	 */
	private void populatePrice(final Price priceModel, final SavedHolidayExtraViewData target)
	{
		final Money moneyModel = priceModel.getAmount();
		if (moneyModel != null && moneyModel.getAmount() != null)
		{
			final BigDecimal totalPrice = moneyModel.getAmount();
			target.setPrice(totalPrice);
			target.setCurrencyAppendedPrice(getCurrencyAppendedPrice(totalPrice,
					Currency.getInstance(Config.getString("default.currency", "GBP"))));
		}
	}

	/**
	 * @param source
	 * 
	 */
	/*
	 * private void populateSummaryDescription(ExtraFacility source, SavedHolidayExtraViewData target) {
	 * 
	 * // boolean description = true; //description = populateFlightExtraDescriptions(source, target); // description =
	 * populateLateCheckoutDescriptions(source, target); // description = populateAttractionExcursionDescriptions(source,
	 * target); //description = populateTransferOptionDescriptions(source, target); // description =
	 * populateInfantEquipmentDescriptions(source, target); if (!description) {
	 * target.setSummaryDescription(target.getDescription()); }
	 * 
	 * }
	 */
	/* *//**
	 * Populate infant equipment descriptions.
	 * 
	 * @param source
	 *           the source
	 * @param target
	 *           the target
	 * @return description
	 */
	/*
	 * private boolean populateInfantEquipmentDescriptions( ExtraFacility source, SavedHolidayExtraViewData target) {
	 * boolean description = false; if (StringUtils.equalsIgnoreCase(source.getExFacilityCategory() .getCode(),
	 * SavedHolidayExtraConstants.INFANT_OPTION_CATEGORY)) { description = true;
	 * target.setSummaryDescription(getDescription(source .getExFacilityCategory().getCode(), source.getDescription(),
	 * Integer.toString(target .getSelectedQuantity()))); }
	 * 
	 * return description; }
	 */
	/**
	 * To set the descriptions related to LateCheckout.
	 * 
	 * @param source
	 * @param target
	 * @return description
	 */
	/*
	 * private boolean populateLateCheckoutDescriptions(ExtraFacility source, SavedHolidayExtraViewData target) { boolean
	 * description = false; if (StringUtils.equalsIgnoreCase(source.getExtraFacilityCode(),
	 * SavedHolidayExtraConstants.LCD)) { description = true;
	 * target.setSummaryDescription(getLateCheckoutDescription(source, target)); } return description; }
	 */
	/**
	 * To set the descriptions related to TransferOption.
	 * 
	 * @param source
	 * @param target
	 * @return description
	 */
	/*
	 * private boolean populateTransferOptionDescriptions( ExtraFacility source, SavedHolidayExtraViewData target) {
	 * boolean description = false; if (CustomeraccountConstants.TRANSFER_OPTION_CODES.contains(source
	 * .getExtraFacilityCode()) || StringUtils.equalsIgnoreCase(source.getExFacilityCategory() .getSuperCategoryCode(),
	 * SavedHolidayExtraConstants.CAR_HIRE)) { description = true;
	 * target.setSummaryDescription(getTransferDescription(source)); } return description; }
	 *//**
	 * To set the descriptions related to Attraction & Excursion.
	 * 
	 * @param source
	 * @param target
	 * @return description
	 */
	/*
	 * private boolean populateAttractionExcursionDescriptions( ExtraFacility source, SavedHolidayExtraViewData target) {
	 * boolean description = false; if (source.getExFacilityCategory() != null &&
	 * StringUtils.equalsIgnoreCase(source.getExFacilityCategory() .getSuperCategoryCode(),
	 * SavedHolidayExtraConstants.EXCURSION) || StringUtils.equalsIgnoreCase(source.getExFacilityCategory()
	 * .getSuperCategoryCode(), SavedHolidayExtraConstants.ATTRACTION)) { description = true;
	 * target.setSummaryDescription(getDescription(source .getExFacilityCategory().getSuperCategoryCode(), source
	 * .getExFacilityCategory().getCode(), (Integer .toString(target.getSelectedQuantity()) + target .getPaxType()))); }
	 * return description; }
	 */

	/* *//**
	 * To set the descriptions related to Flight Extras.
	 * 
	 * @param source
	 * @param target
	 * @return description
	 */
	/*
	 * private boolean populateFlightExtraDescriptions(ExtraFacility source, SavedHolidayExtraViewData target) { boolean
	 * description = false; //int countFlightExtras = StringUtils.countMatches(getStaticContent() //
	 * .get("FLIGHTEXTRAS"), source.getExFacilityGroup().getCode()); // if (countFlightExtras >= 1 // &&
	 * CollectionUtils.isNotEmpty(source.getPassengers())) { description = true;
	 * target.setSummaryDescription(getFlightExtrasDescription(source)); // } return description; }
	 */

	/* *//**
	 * Gets the flight extras description.
	 * 
	 * @param source
	 *           the source
	 * @return the flight extras description
	 */
	/*
	 * private String getFlightExtrasDescription(ExtraFacility source) { if
	 * (StringUtils.equalsIgnoreCase(source.getExFacilityCategory() .getCode(),
	 * SavedHolidayExtraConstants.BAGGAGE_EXTRA_CATEGORY_CODE)) { return getDescription(
	 * SavedHolidayExtraConstants.BAGGAGE_EXTRA_CATEGORY_CODE, ((BaggageExtraFacility) source).getBaggageWeight()
	 * .toString(), Integer.toString(source.getPassengers().size())); } else { return getDescription("EXTRADESCRIPTION",
	 * source.getDescription(), Integer.toString(source.getPassengers().size())); } }
	 *//**
	 * Gets the late checkout description.
	 * 
	 * @param source
	 *           the source
	 * @param target
	 *           the target
	 * @return the late checkout description
	 */
	/*
	 * private String getLateCheckoutDescription(ExtraFacility source, SavedHolidayExtraViewData target) { if
	 * (target.getSelectedQuantity() <= 1) { return getDescription(source.getExtraFacilityCode(), Integer.toString(target
	 * .getSelectedQuantity())); } else { return getDescription("LCDS", Integer.toString(target .getSelectedQuantity()));
	 * } }
	 */
	/**
	 * Decides if the extra facility is included along with the package.
	 * 
	 * @param extraFacilityModel
	 * @return boolean
	 */
	private boolean isExtraIncluded(final ExtraFacility extraFacilityModel)
	{
		return extraFacilityModel.getSelection() == FacilitySelectionType.INCLUDED;
	}

	/**
	 * Decides if the extra facility is free along with the package.
	 * 
	 * @param extraFacilityModel
	 * @return boolean
	 */
	private boolean isExtraFree(final ExtraFacility extraFacilityModel)
	{
		return extraFacilityModel.getSelection() == FacilitySelectionType.FREE;
	}

	/**
	 * Gets the currency appended child price.
	 * 
	 * @param price
	 *           the price
	 * @param currency
	 *           the currency
	 * @return the currency appended child price
	 */
	private String getCurrencyAppendedPrice(final BigDecimal price, final Currency currency)
	{
		if (price.intValue() >= ShortlistConstants.ONE)
		{
			return currency.getSymbol() + price.intValue();
		}
		return StringUtils.EMPTY;
	}

	/**
	 * Populate descrption.
	 * 
	 * @param source
	 *           the source
	 * @return the string
	 */
	private String populateDescrption(final ExtraFacility source)
	{

		final int countCategoryCodes = StringUtils.countMatches(getDescriptionFromProperties("CATEGORY_CODES"), source
				.getExtraFacilityCategory().getCode());
		if (countCategoryCodes >= 1)
		{
			return getDescription(source.getExtraFacilityCategory().getCode());
		}

		final int countExtraFacilityCodes = StringUtils.countMatches(getDescriptionFromProperties("EXTRAFACILITY_CODES"),
				source.getExtraFacilityCode());
		if (countExtraFacilityCodes >= 1)
		{
			return getDescription(source.getExtraFacilityCode());
		}

		return source.getDescription();
	}

	/**
	 * Gets the description from property reader.
	 * 
	 * @param key
	 *           the key
	 * @param value
	 *           the value
	 * @return the description
	 */
	private String getDescriptionFromProperties(final String key, final String... value)
	{
		//if (key != null)
		//{
			//if (SyntacticSugar.isNull(value))
			//{
				//return caPropertyReader.getValue(key);
			//}
			//return caPropertyReader.substitute(key, value);
		//}
		return null;
	}


	/**
	 * Gets the description from content csv.
	 * 
	 * @param key
	 *           the key
	 * @param value
	 *           the value
	 * @return the description
	 */
	private String getDescription(final String key, final String... value)
	{
		//if (key != null)
		//{
			//String summaryContent = getStaticContent().get(key);
			//if (SyntacticSugar.isNull(value))
			//{
				//return summaryContent;
			//}
			//return StringUtil.substitute(summaryContent, value);
		//}
		return null;
	}


}
