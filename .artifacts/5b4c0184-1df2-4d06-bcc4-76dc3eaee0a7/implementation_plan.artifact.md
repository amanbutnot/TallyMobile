# Add Delivery Time Section to Cart Screen

Add a new "Delivery Time" card section in the `CartScreen.kt` file, positioned underneath the Order Summary (Overview). This section will allow users to select a delivery day (Today or Tomorrow) and specify a time slot (Start Time and End Time).

## User Review Required

> [!IMPORTANT]
> The "Confirm Order" button is currently inside the `CartSummary` card. To place the "Delivery Time" section underneath the summary while keeping the button at the bottom, I will need to move the button logic out of `CartSummary` or place the new section before the button. I propose placing the "Delivery Time" section between the "Order Summary" card and the "Confirm Order" button for better flow.

## Proposed Changes

### [Component: UI]

#### [MODIFY] [CartScreen.kt](file:///Users/aman/Projects/Freelance/Easy%20Karobar/composeApp/src/commonMain/kotlin/org/prime/easykarobar/ui/screen/distributor/order/CartScreen.kt)

- **Create `DeliveryTimeSection` Composable:**
    - A `Card` with consistent styling (shadow, rounded corners, border).
    - Title: "Delivery Time".
    - Delivery Day selection: `Row` with "Today" and "Tomorrow" using `RadioButton` and `Text`.
    - Time Slot selection: `Row` with two `TallyTextField`s or simple text boxes for "Start Time" and "End Time".
- **Update `CartContent`:**
    - Add state for `deliveryDay`, `startTime`, and `endTime`.
    - Integrate `DeliveryTimeSection` into the `LazyColumn`.
    - Reposition the "Confirm Order" button if necessary to ensure it's at the bottom of all sections.

## Verification Plan

### Manual Verification
- Deploy the app and navigate to the Cart Screen.
- Verify the "Delivery Time" card appears below the Order Summary.
- Verify radio buttons for "Today" and "Tomorrow" work (UI toggle).
- Verify the time slot fields are visible and editable.
