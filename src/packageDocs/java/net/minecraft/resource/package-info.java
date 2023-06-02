/*
 * This file is free for everyone to use under the Creative Commons Zero license.
 */

/**
 * Provides resources to Minecraft, including resource access and provision.
 * <p>
 * "Data" as in "Data Packs" is considered resource as well.
 * <p>
 * Here is a quick overview on the resource access and provision APIs of Minecraft:
 * <div class="quilt" id="resource-access"><table border=1>
 * <caption>Resource Access APIs</caption>
 * <tr>
 *     <th><b>Class</b></th><th><b>Usage</b></th>
 * </tr>
 * <tr>
 *     <td>{@link Resource}</td>
 *     <td>Accesses binary data.</td>
 * </tr>
 * <tr>
 *     <td>{@link ResourceFactory}</td>
 *     <td>Provides a resource given an {@link net.minecraft.util.Identifier}.</td>

 * </tr>
 * <tr>
 *     <td>{@link ResourceManager}</td>
 *     <td>Exposes more resource access in addition to being a {@link ResourceFactory}.</td>
 * </tr>
 * <tr>
 *     <td>{@link ResourceReloader}</td>
 *     <td>The most common accessor to resources, acting during "reloads" to set up in-game contents.
 *     <br><i>This is usually implemented by mods using resources.</i></td>
 * </tr>
 * <tr>
 *     <td>{@link ReloadableResourceManager}</td>
 *     <td>Performs reloads and manages {@link ResourceReloader}s in addition to being a {@link ResourceManager}.
 *     <br>Usually held by the game engine, it may be provided by the modding APIs as well.</td>
 * </tr>
 * </table></div>
 *
 * <div class="quilt" id="resource-provision"><table border=1>
 * <caption>Resource Provision APIs</caption>
 * <tr>
 *     <th><b>Class</b></th><th><b>Usage</b></th>
 * </tr>
 * <tr>
 *     <td>{@link ResourcePack}</td>
 *     <td>Provides binary data based on queries.
 *     <br>They are usually single-use, created by {@link ResourcePackManager} and provided
 *     to {@link ReloadableResourceManager} in each reload.</td>
 * </tr>
 * <tr>
 *     <td>{@link ResourcePackProfile}</td>
 *     <td>A user-friendly, persistent form of {@link ResourcePack}. Used to create resource
 *     packs in reloads.</td>
 * </tr>
 * <tr>
 *     <td>{@link ResourcePackProvider}</td>
 *     <td>Provides {@link ResourcePackProfile}s, so they are taken account of during reloads.
 *     <br><i>This is usually implemented by mods providing resources.</i></td>
 * </tr>
 * <tr>
 *     <td>{@link ResourcePackManager}</td>
 *     <td>Keeps track of {@link ResourcePackProvider}s and uses the profiles from the providers
 *     to create {@link ResourcePack}s to send to {@link ReloadableResourceManager}s in each reload.</td>
 * </tr>
 * </table></div>
 * <p>
 * In addition to these APIs, this package includes implementation details of the resource system.
 */

package net.minecraft.resource;

import net.minecraft.resource.pack.*;
